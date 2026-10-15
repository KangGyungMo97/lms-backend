package com.hitech.lms.domain.study;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.course.CourseRepository;
import com.hitech.lms.domain.course.Lesson;
import com.hitech.lms.domain.course.LessonRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttService {

	public static final int OPEN_MINUTES = 10;   // 시작 후 10분 동안만 입력 가능
	public static final int MAX_FAIL = 5;        // 틀릴 수 있는 횟수

	private static final SecureRandom RANDOM = new SecureRandom();   // 예측하기 어려운 난수

	private final AttRepository ar;

	private final AttSessionRepository sr;

	private final AttFailRepository fr;

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

	// ===== 교수: 출석 시작 =====

	// 내가 담당하는 수업 목록 (출석 시작 버튼이 붙는 목록)
	public List<Lesson> getMyLessons(Users teacher) {
		return lr.findByCourse_Teacher_UserIdOrderByLessonIdDesc(teacher.getUserId());
	}

	// 이 수업의 출석 세션 - 아직 시작 안 했으면 비어 있음
	public Optional<AttSession> findSession(Lesson lesson) {
		return sr.findByLesson(lesson);
	}

	// 수업 목록의 세션 { 수업번호 : 세션 } - 목록에서 '미시작 / 진행중 / 마감' 표시용
	public Map<Long, AttSession> getSessionMap(List<Lesson> lessons) {
		Map<Long, AttSession> map = new HashMap<>();
		if (lessons.isEmpty()) return map;
		for (AttSession session : sr.findByLessonIn(lessons)) {
			map.put(session.getLesson().getLessonId(), session);
		}
		return map;
	}

	// 이 수업에 출석한 학생 목록 (먼저 입력한 순서)
	public List<Att> getLessonAtts(Lesson lesson) {
		return ar.findByLessonOrderByAttDateAsc(lesson);
	}

	// 출석 시작 - 6자리 코드 만들고, 마감은 지금 + 10분
	// 이미 시작한 수업이면 새로 만들지 않고 있던 걸 돌려줌 (코드가 바뀌면 안 되니까)
	@Transactional
	public AttSession start(Lesson lesson) {
		Optional<AttSession> old = sr.findByLesson(lesson);
		if (old.isPresent()) {
			return old.get();
		}

		LocalDateTime now = LocalDateTime.now();

		AttSession session = new AttSession();
		session.setLesson(lesson);
		session.setCode(String.format("%06d", RANDOM.nextInt(1_000_000)));   // 000000 ~ 999999
		session.setOpenedAt(now);
		session.setExpiresAt(now.plusMinutes(OPEN_MINUTES));

		return sr.save(session);
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
	//   학생 명단 = 과목의 학과와 같은 학과 학생 + 이 과정에 출석 기록이 있는 학생
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
		for (Users student : ar.findStudentsOfDept(course.getSubject().getDept())) {
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

	// ===== 학생 =====

	// 지금 열려 있는 출석 세션 (마감 전)
	// TODO: ERD 에 수강신청 테이블이 없음 → 생기면 '내가 듣는 과정의 세션만' 으로 좁히기
	public List<AttSession> getOpenSessions() {
		return sr.findByExpiresAtAfterOrderByOpenedAtDesc(LocalDateTime.now());
	}

	// 내 출석 기록 (최근 순)
	public List<Att> getMyAtts(Users student) {
		return ar.findByStudentOrderByAttDateDesc(student);
	}

	// 세션별 내가 틀린 횟수 { 세션번호 : 횟수 } - 한 번도 안 틀렸으면 키가 없음
	public Map<Long, Integer> getFailCountMap(Users student, List<AttSession> sessions) {
		Map<Long, Integer> map = new HashMap<>();
		if (sessions.isEmpty()) return map;
		for (AttFail fail : fr.findByStudentAndSessionIn(student, sessions)) {
			map.put(fail.getSession().getSessionId(), fail.getFailCount());
		}
		return map;
	}

	// 코드 입력 → 출석 처리
	//   순서: 시작했나 → 이미 기록이 있나 → 닫혔나 → 잠겼나 → 코드 비교
	//   맞으면 Att(Y) 저장 / 틀리면 틀린 횟수 +1 (5회가 되면 이번 수업은 잠김)
	@Transactional
	public AttResult check(Lesson lesson, Users student, String input) {

		AttSession session = sr.findByLesson(lesson).orElse(null);
		if (session == null) {
			return AttResult.of(AttResult.Status.NOT_STARTED);
		}

		// 이미 기록이 있으면 코드는 받지 않음 (교수가 수정한 기록을 학생이 덮어쓰지 못하게)
		Att old = ar.findByStudentAndLesson(student, lesson).orElse(null);
		if (old != null) {
			boolean attended = "Y".equals(old.getAttStatus()) || "L".equals(old.getAttStatus());
			return AttResult.of(attended ? AttResult.Status.ALREADY : AttResult.Status.PROCESSED);
		}

		LocalDateTime now = LocalDateTime.now();
		if (!now.isBefore(session.getExpiresAt())) {
			return AttResult.of(AttResult.Status.CLOSED);
		}

		AttFail fail = fr.findBySessionAndStudent(session, student).orElse(null);
		int failCount = (fail == null) ? 0 : fail.getFailCount();
		if (failCount >= MAX_FAIL) {
			return AttResult.of(AttResult.Status.LOCKED);   // 코드가 맞아도 잠긴 뒤에는 출석 불가
		}

		// 코드 비교 (글자 수가 같을 때 걸리는 시간이 일정한 비교)
		boolean match = MessageDigest.isEqual(
				session.getCode().getBytes(StandardCharsets.UTF_8),
				input.getBytes(StandardCharsets.UTF_8));

		if (match) {
			Att att = new Att();
			att.setStudent(student);
			att.setLesson(lesson);
			att.setAttStatus("Y");          // 출석 (10분 안에만 입력할 수 있으므로 코드로는 지각 L 이 없음)
			att.setAttDate(now);
			ar.save(att);
			return AttResult.of(AttResult.Status.SUCCESS);
		}

		// 틀림 → 횟수 +1
		if (fail == null) {
			fail = new AttFail();
			fail.setSession(session);
			fail.setStudent(student);
		}
		fail.setFailCount(failCount + 1);
		fr.save(fail);

		int remaining = MAX_FAIL - fail.getFailCount();
		if (remaining <= 0) {
			return AttResult.of(AttResult.Status.LOCKED);
		}
		return new AttResult(AttResult.Status.WRONG, remaining);
	}
}
