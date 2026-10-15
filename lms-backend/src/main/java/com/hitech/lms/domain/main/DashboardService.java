package com.hitech.lms.domain.main;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.board.Notice;
import com.hitech.lms.domain.course.ClassClock;
import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.qna.QnaBoard;
import com.hitech.lms.domain.study.Att;
import com.hitech.lms.domain.study.AttService;
import com.hitech.lms.domain.study.AttSession;
import com.hitech.lms.domain.study.Homework;
import com.hitech.lms.domain.study.Submit;
import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;

// 대시보드 = 요약 화면 (전체 목록은 각 메뉴에서)
//   학생: 요약 숫자 4개 · 오늘의 수업 · 수강 중 과목 · 마감 임박 과제 · 공지사항
//   교수: 요약 숫자 4개 · 오늘의 수업 · 담당 과목 · 채점 대기 과제 · 미답변 Q&A
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

	public static final int LIMIT = 5;    // 카드마다 보여 줄 줄 수

	private final DashboardRepository dr;

	private final AttService as;

	// ===== 공통 =====

	// "2026학년도 2학기" (1학기 3~8월 / 2학기 9월~다음해 2월)
	public String semesterLabel() {
		LocalDate t = LocalDate.now();
		int m = t.getMonthValue();
		int year = m >= 3 ? t.getYear() : t.getYear() - 1;
		int sem = (m >= 3 && m <= 8) ? 1 : 2;
		return year + "학년도 " + sem + "학기";
	}

	// 최근 과목 공지
	public List<Notice> getNotices() {
		return dr.findRecentNotices(PageRequest.of(0, LIMIT));
	}

	// 강의 한 줄 - 진도율은 '강의 기간 중 오늘까지 지난 날짜' 비율
	private DashCourseRow toRow(Course c, int rosterCount) {
		LocalDate today = LocalDate.now();
		LocalDate from = c.getCourseStartDate(), to = c.getCourseEndDate();

		String state = "진행 중";
		int progress = 0;
		if (from != null && to != null) {
			if (today.isBefore(from)) {
				state = "예정";
			} else if (today.isAfter(to)) {
				state = "종료";
				progress = 100;
			} else {
				long total = ChronoUnit.DAYS.between(from, to) + 1;
				long passed = ChronoUnit.DAYS.between(from, today) + 1;
				progress = (int) Math.round(passed * 100.0 / total);
			}
		}
		return new DashCourseRow(c, ClassClock.label(c), progress, state, rosterCount);
	}

	// ===== 학생 =====

	public List<Course> getStudentCourses(Users student) {
		return as.getStudentCourses(student);
	}

	public List<DashCourseRow> getStudentCourseRows(List<Course> courses) {
		List<DashCourseRow> rows = new ArrayList<>();
		for (Course c : courses) rows.add(toRow(c, 0));
		return rows;
	}

	// 과제 - 마감 안 지난 것 먼저(마감 빠른 순), 그다음 최근 7일 안에 마감된 것
	public List<DashHomeworkRow> getStudentHomeworks(Users student, List<Course> courses) {
		if (courses.isEmpty()) return List.of();

		LocalDate today = LocalDate.now();
		List<Homework> homeworks = dr.findHomeworks(courses).stream()
				.filter(h -> !h.getHomeworkEndDate().isBefore(today.minusDays(7)))
				.toList();
		if (homeworks.isEmpty()) return List.of();

		Map<Long, Submit> mine = new HashMap<>();
		for (Submit s : dr.findMySubmits(student, homeworks)) {
			mine.put(s.getHomework().getHomeworkId(), s);
		}

		List<DashHomeworkRow> open = new ArrayList<>(), closed = new ArrayList<>();
		for (Homework h : homeworks) {
			Submit s = mine.get(h.getHomeworkId());
			boolean past = h.getHomeworkEndDate().isBefore(today);
			String state;
			if (s == null || "N".equals(s.getSubmitIsDone())) {
				state = past ? "마감" : "미제출";
			} else {
				state = switch (s.getSubmitIsDone()) {
					case "R" -> "채점완료";
					case "F" -> "마감";
					default  -> "제출완료";
				};
			}
			(past ? closed : open).add(new DashHomeworkRow(h, state, 0, 0, 0));
		}
		closed.sort(Comparator.comparing((DashHomeworkRow r) -> r.getHomework().getHomeworkEndDate()).reversed());
		open.addAll(closed);
		return open;
	}

	// 미제출 과제 수 (마감 안 지난 것만)
	public long countUnsubmitted(List<DashHomeworkRow> rows) {
		return rows.stream().filter(r -> "미제출".equals(r.getState())).count();
	}

	// 이번 달 출석률 % = (출석 + 지각 + 공결) / 이번 달 진행한 수업 수 (진행한 수업이 없으면 null)
	public Integer getMonthAttendRate(Users student, List<Course> courses) {
		if (courses.isEmpty()) return null;

		LocalDate first = LocalDate.now().withDayOfMonth(1);
		List<AttSession> sessions = dr.findSessions(courses, first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
		if (sessions.isEmpty()) return null;

		Set<Long> lessonIds = new HashSet<>();
		for (AttSession s : sessions) lessonIds.add(s.getLesson().getLessonId());

		long attended = 0;
		for (Att a : as.getMyAtts(student)) {
			if (lessonIds.contains(a.getLesson().getLessonId()) && !"N".equals(a.getAttStatus())) attended++;
		}
		return (int) Math.round(attended * 100.0 / lessonIds.size());
	}

	// ===== 교수 =====

	public List<Course> getTeacherCourses(Users teacher) {
		return as.getTeacherCourses(teacher);
	}

	public List<DashCourseRow> getTeacherCourseRows(List<Course> courses, Map<Long, Integer> rosterMap) {
		List<DashCourseRow> rows = new ArrayList<>();
		for (Course c : courses) rows.add(toRow(c, rosterMap.getOrDefault(c.getCourseId(), 0)));
		return rows;
	}

	// 강의별 수강 인원 { 강의번호 : 인원 }
	public Map<Long, Integer> getRosterMap(List<Course> courses) {
		Map<Long, Integer> map = new HashMap<>();
		for (Course c : courses) map.put(c.getCourseId(), as.getRoster(c).size());
		return map;
	}

	// 과제 - 채점 안 한 제출물이 많은 순 → 마감 빠른 순
	//   채점 대기가 없는 지난 과제는 14일이 지나면 빼고 보여 줌
	public List<DashHomeworkRow> getTeacherHomeworks(List<Course> courses, Map<Long, Integer> rosterMap) {
		if (courses.isEmpty()) return List.of();

		List<Homework> homeworks = dr.findHomeworks(courses);
		if (homeworks.isEmpty()) return List.of();

		Map<Long, int[]> counts = new HashMap<>();   // { 과제번호 : [제출, 채점 대기] }
		for (Submit s : dr.findSubmits(homeworks)) {
			int[] c = counts.computeIfAbsent(s.getHomework().getHomeworkId(), k -> new int[2]);
			if ("Y".equals(s.getSubmitIsDone()) || "R".equals(s.getSubmitIsDone())) c[0]++;
			if ("Y".equals(s.getSubmitIsDone())) c[1]++;
		}

		LocalDate today = LocalDate.now();
		List<DashHomeworkRow> rows = new ArrayList<>();
		for (Homework h : homeworks) {
			int[] c = counts.getOrDefault(h.getHomeworkId(), new int[2]);
			boolean past = h.getHomeworkEndDate().isBefore(today);
			if (c[1] == 0 && h.getHomeworkEndDate().isBefore(today.minusDays(14))) continue;

			String state = c[1] > 0 ? "채점 대기" : past ? "채점 완료" : "진행 중";
			int roster = rosterMap.getOrDefault(h.getLesson().getCourse().getCourseId(), 0);
			rows.add(new DashHomeworkRow(h, state, c[0], roster, c[1]));
		}
		rows.sort(Comparator.comparing((DashHomeworkRow r) -> -r.getUngraded())
				.thenComparing(r -> r.getHomework().getHomeworkEndDate()));
		return rows;
	}

	// 채점 대기 제출물 수 (모든 과제 합)
	public int countUngraded(List<DashHomeworkRow> rows) {
		return rows.stream().mapToInt(DashHomeworkRow::getUngraded).sum();
	}

	// 답변 대기 질문
	public List<QnaBoard> getWaitingQna(Users teacher) {
		return dr.findWaitingQna(teacher.getUserId());
	}
}
