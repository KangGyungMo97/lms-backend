package com.hitech.lms.domain.study;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.course.ClassClock;
import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.course.CourseRepository;
import com.hitech.lms.domain.course.Lesson;
import com.hitech.lms.domain.course.LessonRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

// 출석 = '수업 시간에 강의실에 들어오면 자동 기록'
//   1) 교수가 수업 시간에 [수업 시작] → 오늘 진행할 차시의 출석 세션이 열림 (수업 끝나는 시각까지)
//   2) 학생이 수업 시간에 [수업 참여] → 들어온 시각으로 출석(Y) / 지각(L) 자동 기록 후 차시 화면으로
//      시작 후 10분까지 출석, 그 뒤로는 지각, 수업이 끝나면 참여 불가 (기록 없으면 출석부에서 결석)
//   ※ 교수가 늦게 시작하면 '교수가 시작한 시각' 기준으로 10분
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttService {

	public static final int ENTER_BEFORE_MINUTES = 10;   // 수업 시작 10분 전부터 입장 가능
	public static final int LATE_MINUTES = 10;           // 시작 후 10분이 지나면 지각

	private static final SecureRandom RANDOM = new SecureRandom();

	private final AttRepository ar;

	private final AttSessionRepository sr;

	private final LessonRepository lr;

	private final CourseRepository cr;

	// ===== 공통 =====

	// 수업 찾기 - 없으면 404
	public Lesson getLesson(Long lessonId) {
		return lr.findById(lessonId)
				.orElseThrow(() -> new DataNotFoundException("수업을 찾을 수 없습니다."));
	}

	// 이 수업을 맡은 교수인지 (수업 → 과정 → 담당 교수)
	public boolean isOwner(Lesson lesson, Users user) {
		return lesson.getCourse().getTeacher().getUserId().equals(user.getUserId());
	}

	// 수강 대상 학생 - 과목의 학과 + 대상 학년 (대상 학년이 비어 있으면 학과 전체)
	// TODO: 수강신청 테이블이 생기면 '이 과정을 신청한 학생' 으로 바꾸기
	// 수강 대상 학생 - 과목의 학과 + 대상 학년 (하이테크 과정 및 일반 과정 분기 처리)
		public List<Users> getRoster(Course course) {
			List<String> targets = new ArrayList<>();
			if (course.getCourseTarget() != null) {
				targets.add(String.valueOf(course.getCourseTarget()));
			} else {
				// 대상 학년 코드가 따로 지정되지 않은 과목이라면 1학년, 2학년, 하이테크(9) 모두 수강 대상으로 포용합니다.
				targets.addAll(List.of("1", "2", "3", "4", "9")); 
			}
			return ar.findStudentsOfDeptAndTargets(course.getSubject().getDept(), targets);
		}

		// 이 학생이 듣는 강의인지 검증 (학과 + 학년/과정 매핑 확인)
		public boolean isTarget(Course course, Users student) {
			if (!"S".equals(student.getUserRole())) return false;
			if (!course.getSubject().getDept().getDeptId().equals(student.getDept().getDeptId())) return false;
			if (course.getCourseTarget() == null) return true;
			
			// 과목 대상과 학생의 학년 정보가 일치하거나, 하이테크(9) 과정인 경우 매핑 성공
			String courseTargetStr = String.valueOf(course.getCourseTarget());
			return courseTargetStr.equals(student.getUserGrade()) || "9".equals(courseTargetStr);
		}


	// 학생이 듣는 강의 목록
		public List<Course> getStudentCourses(Users student) {
			List<Integer> targets = new ArrayList<>();
			targets.add(9); // 하이테크 과정 타겟 코드(9)를 무조건 탐색 리스트에 기본 할당
			
			try {
				if (student.getUserGrade() != null) {
					targets.add(Integer.valueOf(student.getUserGrade()));
				}
			} catch (NumberFormatException e) {
				// 학년 필드 값이 숫자가 아니더라도 에러를 핸들링하며 하이테크(9) 단독 조건으로 조회를 속행합니다.
			}
			return ar.findCoursesOfDeptAndTargets(student.getDept(), targets);
		}

	// 교수가 맡은 강의 목록
	public List<Course> getTeacherCourses(Users teacher) {
		return cr.findByTeacher_UserIdAndCourseIsUseOrderByCourseIdDesc(teacher.getUserId(), "Y");
	}

	// ===== 오늘의 수업 =====

	// 이 강의의 오늘 출석 세션 (교수가 오늘 '수업 시작'을 눌렀으면 있음)
	public Optional<AttSession> findTodaySession(Course course) {
		LocalDate today = LocalDate.now();
		return sr.findByLesson_Course(course).stream()
				.filter(s -> s.getOpenedAt().toLocalDate().equals(today))
				.findFirst();
	}

	// 오늘의 수업 카드 목록 - 오늘 수업이 있는 강의만, 시작 시각 순
	//   student 가 있으면 내 출석 기록을, 없으면(교수) 출석 인원을 채움
	public List<TodayClass> getTodayClasses(List<Course> courses, Users student) {
		List<TodayClass> list = new ArrayList<>();
		for (Course c : courses) {
			Optional<ClassClock.Slot> slot = ClassClock.today(c);
			if (slot.isEmpty()) continue;

			List<Lesson> lessons = ar.findLessonsOfCourse(c);
			List<AttSession> sessions = sr.findByLesson_Course(c);
			AttSession todaySession = sessions.stream()
					.filter(s -> s.getOpenedAt().toLocalDate().equals(LocalDate.now()))
					.findFirst().orElse(null);

			// 시작했으면 그 차시, 아니면 아직 세션이 없는 첫 차시
			Lesson lesson = (todaySession != null) ? todaySession.getLesson() : nextLesson(lessons, sessions);
			int lessonNo = (lesson == null) ? 0 : indexOf(lessons, lesson) + 1;

			Att myAtt = null;
			int attendCount = 0, rosterCount = 0;
			if (student != null) {
				if (todaySession != null) {
					myAtt = ar.findByStudentAndLesson(student, todaySession.getLesson()).orElse(null);
				}
			} else {
				rosterCount = getRoster(c).size();
				if (todaySession != null) {
					attendCount = (int) ar.findByLessonOrderByAttDateAsc(todaySession.getLesson()).stream()
							.filter(a -> "Y".equals(a.getAttStatus()) || "L".equals(a.getAttStatus()))
							.count();
				}
			}
			list.add(new TodayClass(c, slot.get(), lesson, lessonNo, todaySession, myAtt, attendCount, rosterCount));
		}
		list.sort((a, b) -> a.getSlot().start().compareTo(b.getSlot().start()));
		return list;
	}

	// 아직 출석 세션이 없는 첫 차시 (모두 진행했으면 null)
	private Lesson nextLesson(List<Lesson> lessons, List<AttSession> sessions) {
		Set<Long> done = new HashSet<>();
		for (AttSession s : sessions) done.add(s.getLesson().getLessonId());
		for (Lesson l : lessons) {
			if (!done.contains(l.getLessonId())) return l;
		}
		return null;
	}

	private int indexOf(List<Lesson> lessons, Lesson lesson) {
		for (int i = 0; i < lessons.size(); i++) {
			if (lessons.get(i).getLessonId().equals(lesson.getLessonId())) return i;
		}
		return -1;
	}

	// ===== 교수: 수업 시작 =====

	// 수업 시작 - 오늘 수업 시간(시작 10분 전 ~ 끝)에만, 다음 차시의 출석 세션을 엶 (끝나는 시각까지)
	//   이미 오늘 시작했으면 새로 만들지 않고 그 차시를 돌려줌
	@Transactional
	public AttResult start(Course course) {
		Optional<ClassClock.Slot> slot = ClassClock.today(course);
		if (slot.isEmpty()) return AttResult.of(AttResult.Status.NO_CLASS);

		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(slot.get().start().minusMinutes(ENTER_BEFORE_MINUTES))) return AttResult.of(AttResult.Status.TOO_EARLY);
		if (!now.isBefore(slot.get().end())) return AttResult.of(AttResult.Status.CLOSED);

		Optional<AttSession> today = findTodaySession(course);
		if (today.isPresent()) {
			return AttResult.of(AttResult.Status.STARTED, today.get().getLesson().getLessonId());
		}

		Lesson lesson = nextLesson(ar.findLessonsOfCourse(course), sr.findByLesson_Course(course));
		if (lesson == null) return AttResult.of(AttResult.Status.NO_LESSON);

		AttSession session = new AttSession();
		session.setLesson(lesson);
		session.setCode(String.format("%06d", RANDOM.nextInt(1_000_000)));   // 코드 입력은 안 쓰지만 컬럼이 NOT NULL
		session.setOpenedAt(now);
		session.setExpiresAt(slot.get().end());                              // 수업 끝나는 시각까지 참여 가능
		sr.save(session);

		return AttResult.of(AttResult.Status.STARTED, lesson.getLessonId());
	}

	// 수업 기록 화면용 - 차시별 세션 { 수업번호 : 세션 }
	public Map<Long, AttSession> getSessionMap(List<Lesson> lessons) {
		Map<Long, AttSession> map = new HashMap<>();
		if (lessons.isEmpty()) return map;
		for (AttSession session : sr.findByLessonIn(lessons)) {
			map.put(session.getLesson().getLessonId(), session);
		}
		return map;
	}

	// 내가 담당하는 차시 목록 (최근 순)
	public List<Lesson> getMyLessons(Users teacher) {
		return lr.findByCourse_Teacher_UserIdOrderByLessonIdDesc(teacher.getUserId());
	}

	// 이 차시의 출석 세션
	public Optional<AttSession> findSession(Lesson lesson) {
		return sr.findByLesson(lesson);
	}

	// 이 차시에 들어온 학생 (먼저 들어온 순서)
	public List<Att> getLessonAtts(Lesson lesson) {
		return ar.findByLessonOrderByAttDateAsc(lesson);
	}

	// ===== 교수: 출석부 · 수정 =====

	// 과정 찾기 - 없으면 404
	public Course getCourse(Long courseId) {
		return cr.findById(courseId)
				.orElseThrow(() -> new DataNotFoundException("과정을 찾을 수 없습니다."));
	}

	// 이 과정을 맡은 교수인지
	public boolean isCourseOwner(Course course, Users user) {
		return course.getTeacher().getUserId().equals(user.getUserId());
	}

	// 내 수업들이 속한 과정 목록 (중복 없이) - 출석부 바로가기용
	public List<Course> getMyCourses(List<Lesson> lessons) {
		Map<Long, Course> map = new LinkedHashMap<>();
		for (Lesson lesson : lessons) {
			map.putIfAbsent(lesson.getCourse().getCourseId(), lesson.getCourse());
		}
		return new ArrayList<>(map.values());
	}

	// 출석부의 가로 칸 - 이 과정의 수업 회차 (오래된 순)
	public List<Lesson> getCourseLessons(Course course) {
		return ar.findLessonsOfCourse(course);
	}

	// 출석부 - 학생 한 줄마다 수업별 상태 + 출석/지각/공결/결석 횟수
	//   학생 명단 = 수강 대상 학생(학과 + 대상 학년) + 이 과정에 출석 기록이 있는 학생
	//   칸의 상태 = 기록이 있으면 그 상태 / 기록이 없는데 수업을 진행했으면 결석(N) / 진행 전이면 -
	public List<AttBookRow> getBook(Course course, List<Lesson> lessons) {

		List<Att> atts = ar.findByLesson_Course(course);

		// 출석 세션이 있는 수업 = 진행한 수업
		Set<Long> startedLessonIds = new HashSet<>();
		for (AttSession session : sr.findByLesson_Course(course)) {
			startedLessonIds.add(session.getLesson().getLessonId());
		}

		// 학생 명단 (학번 순)
		Map<String, Users> roster = new TreeMap<>();
		for (Users student : getRoster(course)) {
			roster.put(student.getUserId(), student);
		}
		for (Att att : atts) {
			roster.putIfAbsent(att.getStudent().getUserId(), att.getStudent());
		}

		// { 학번 : { 수업번호 : 상태 } }
		Map<String, Map<Long, String>> recorded = new HashMap<>();
		for (Att att : atts) {
			recorded.computeIfAbsent(att.getStudent().getUserId(), k -> new HashMap<>())
					.put(att.getLesson().getLessonId(), att.getAttStatus());
		}

		List<AttBookRow> rows = new ArrayList<>();
		for (Users student : roster.values()) {
			Map<Long, String> mine = recorded.getOrDefault(student.getUserId(), Map.of());
			Map<Long, String> cells = new LinkedHashMap<>();
			int present = 0, late = 0, excused = 0, absent = 0;

			for (Lesson lesson : lessons) {
				String status = mine.get(lesson.getLessonId());
				if (status == null) {
					status = startedLessonIds.contains(lesson.getLessonId()) ? "N" : "-";
				}
				cells.put(lesson.getLessonId(), status);

				switch (status) {
					case "Y" -> present++;
					case "L" -> late++;
					case "E" -> excused++;
					case "N" -> absent++;
					default -> { }   // '-' 는 세지 않음
				}
			}
			rows.add(new AttBookRow(student, cells, present, late, excused, absent));
		}
		return rows;
	}

	// 수업별 날짜 { 수업번호 : 날짜 } - 달력에서 어느 날에 표시할지
	//   출석 세션이 있으면 '출석 시작한 날' / 세션 없이 교수가 직접 기록만 한 수업은 가장 이른 기록의 날 / 둘 다 없으면 키 없음
	//   ※ Lesson 에 날짜 컬럼이 없어서 이렇게 정함
	public Map<Long, LocalDate> getLessonDates(Course course) {
		Map<Long, LocalDate> map = new HashMap<>();
		for (Att att : ar.findByLesson_Course(course)) {
			map.merge(att.getLesson().getLessonId(), att.getAttDate().toLocalDate(),
					(a, b) -> a.isBefore(b) ? a : b);
		}
		for (AttSession session : sr.findByLesson_Course(course)) {
			map.put(session.getLesson().getLessonId(), session.getOpenedAt().toLocalDate());
		}
		return map;
	}

	// 월 달력 - 일요일 시작, 앞뒤 빈 칸은 지난달·다음달 날짜로 채운 날짜 칸 목록 (7개씩 한 주)
	//   칸마다 그 날 진행한 수업과 출석/지각/공결/결석 인원수가 들어감
	public List<AttCalDay> getMonth(List<Lesson> lessons, List<AttBookRow> rows,
			Map<Long, LocalDate> dates, YearMonth ym) {

		// 날짜별 수업 + 인원수
		Map<LocalDate, List<AttCalLesson>> byDate = new HashMap<>();
		for (Lesson lesson : lessons) {
			LocalDate date = dates.get(lesson.getLessonId());
			if (date == null) continue;                       // 날짜 없는 수업은 달력에 안 나옴

			int present = 0, late = 0, excused = 0, absent = 0;
			for (AttBookRow row : rows) {
				String status = row.getCells().get(lesson.getLessonId());
				if (status == null) continue;
				switch (status) {
					case "Y" -> present++;
					case "L" -> late++;
					case "E" -> excused++;
					case "N" -> absent++;
					default -> { }
				}
			}
			byDate.computeIfAbsent(date, k -> new ArrayList<>())
					.add(new AttCalLesson(lesson.getLessonId(), lesson.getLessonTitle(), present, late, excused, absent));
		}

		// 달력 칸 - 첫 주는 일요일부터, 마지막 주는 토요일까지 (getValue: 월 1 ~ 일 7 → % 7 하면 일 0 ~ 토 6)
		LocalDate first = ym.atDay(1);
		LocalDate start = first.minusDays(first.getDayOfWeek().getValue() % 7);
		LocalDate last = ym.atEndOfMonth();
		LocalDate end = last.plusDays(6 - (last.getDayOfWeek().getValue() % 7));
		LocalDate today = LocalDate.now();

		List<AttCalDay> days = new ArrayList<>();
		for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
			days.add(new AttCalDay(d, YearMonth.from(d).equals(ym), d.equals(today),
					byDate.getOrDefault(d, List.of())));
		}
		return days;
	}

	// 출석 상태 수정 - status : Y 출석 / L 지각 / E 공결 / N 결석
	//   기록이 있으면 상태만 바꾸고(출석 시각은 그대로),
	//   기록이 없으면 새로 만듦 (시각 = 수업 시작 시각, 시작 안 한 수업이면 지금)
	@Transactional
	public void modify(Lesson lesson, Users student, String status) {

		Att att = ar.findByStudentAndLesson(student, lesson).orElse(null);

		if (att == null) {
			att = new Att();
			att.setStudent(student);
			att.setLesson(lesson);
			att.setAttDate(sr.findByLesson(lesson)
					.map(AttSession::getOpenedAt)
					.orElse(LocalDateTime.now()));
		}
		att.setAttStatus(status);

		ar.save(att);
	}

	// ===== 학생: 수업 참여 =====

	// 수업 참여 → 들어온 시각으로 출석 / 지각 자동 기록
	//   순서: 내 강의인가 → 오늘 수업 있나 → 입장 시간인가 → 교수가 시작했나 → 이미 기록 있나 → 출석/지각 저장
	@Transactional
	public AttResult enter(Course course, Users student) {
		if (!isTarget(course, student)) return AttResult.of(AttResult.Status.NOT_MINE);

		Optional<ClassClock.Slot> slot = ClassClock.today(course);
		if (slot.isEmpty()) return AttResult.of(AttResult.Status.NO_CLASS);

		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(slot.get().start().minusMinutes(ENTER_BEFORE_MINUTES))) return AttResult.of(AttResult.Status.TOO_EARLY);

		AttSession session = findTodaySession(course).orElse(null);
		if (session == null) {
			return AttResult.of(now.isBefore(slot.get().end()) ? AttResult.Status.NOT_STARTED : AttResult.Status.CLOSED);
		}
		Lesson lesson = session.getLesson();

		// 이미 기록이 있으면 다시 쓰지 않음 (교수가 수정한 기록을 덮어쓰지 않게) → 그냥 입장
		Att old = ar.findByStudentAndLesson(student, lesson).orElse(null);
		if (old != null) {
			boolean attended = "Y".equals(old.getAttStatus()) || "L".equals(old.getAttStatus());
			return AttResult.of(attended ? AttResult.Status.ALREADY : AttResult.Status.PROCESSED, lesson.getLessonId());
		}

		if (!now.isBefore(slot.get().end())) return AttResult.of(AttResult.Status.CLOSED);

		// 출석 기준 = 수업 시작 시각과 교수가 시작한 시각 중 늦은 쪽 + 10분
		LocalDateTime base = session.getOpenedAt().isAfter(slot.get().start()) ? session.getOpenedAt() : slot.get().start();
		boolean late = now.isAfter(base.plusMinutes(LATE_MINUTES));

		Att att = new Att();
		att.setStudent(student);
		att.setLesson(lesson);
		att.setAttStatus(late ? "L" : "Y");
		att.setAttDate(now);
		ar.save(att);

		return AttResult.of(late ? AttResult.Status.LATE : AttResult.Status.PRESENT, lesson.getLessonId());
	}

	// 내 출석 기록 (최근 순)
	public List<Att> getMyAtts(Users student) {
		return ar.findByStudentOrderByAttDateDesc(student);
	}
}
