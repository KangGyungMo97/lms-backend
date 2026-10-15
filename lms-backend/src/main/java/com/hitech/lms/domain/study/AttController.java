package com.hitech.lms.domain.study;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.course.Lesson;
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/attendance")
public class AttController {

	private final AttService as;

	private final UserService us;

	// SecurityConfig 의 로그인 페이지 주소와 같게 (/user/login)
	private static final String LOGIN_REDIRECT = "redirect:/user/login";

	// 출석 첫 화면 - 학생: 지금 출석 가능한 수업 + 내 기록 / 교수: 출석부 바로가기 + 내 수업(출석 시작)
	@PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
	@GetMapping("/list")
	public String list(Model model, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Users user = us.getUser(principal.getName());

		// 교수
		if (isTeacher(principal)) {
			List<Lesson> lessonList = as.getMyLessons(user);
			model.addAttribute("lessonList", lessonList);
			model.addAttribute("courseList", as.getMyCourses(lessonList));    // 출석부 바로가기
			model.addAttribute("sessionMap", as.getSessionMap(lessonList));   // { 수업번호 : 세션 }
			return "attendance/teacher";
		}

		// 학생
		List<AttSession> openSessions = as.getOpenSessions();
		List<Att> myAtts = as.getMyAtts(user);
		// { 수업번호 : 내 상태 } - Y 출석 / L 지각 / E 공결 / N 결석 (기록 없으면 키 없음)
		Map<Long, String> myStatus = myAtts.stream()
				.collect(Collectors.toMap(a -> a.getLesson().getLessonId(), Att::getAttStatus, (a, b) -> a));

		model.addAttribute("openSessions", openSessions);
		model.addAttribute("myStatus", myStatus);
		model.addAttribute("failCount", as.getFailCountMap(user, openSessions));       // { 세션번호 : 틀린 횟수 }
		model.addAttribute("maxFail", AttService.MAX_FAIL);
		model.addAttribute("myAtts", myAtts);
		return "attendance/student";
	}

	// ===== 교수: 출석 시작 =====

	// 출석 시작 - 코드를 만들고 코드 화면으로
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/start/{lessonId}")
	public String start(@PathVariable("lessonId") Long lessonId, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Lesson lesson = as.getLesson(lessonId);
		checkOwner(lesson, principal);

		try {
			as.start(lesson);
		} catch (DataIntegrityViolationException e) {
			// 버튼을 동시에 두 번 눌러 이미 만들어진 경우 → 그대로 코드 화면으로
		}
		return "redirect:/attendance/session/" + lessonId;
	}

	// 출석 코드 · 출석한 학생 화면 - 이 수업을 맡은 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/session/{lessonId}")
	public String session(Model model, @PathVariable("lessonId") Long lessonId, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Lesson lesson = as.getLesson(lessonId);
		checkOwner(lesson, principal);

		AttSession session = as.findSession(lesson).orElse(null);
		if (session == null) {
			return "redirect:/attendance/list";    // 아직 시작 안 했으면 목록으로
		}

		model.addAttribute("lesson", lesson);
		model.addAttribute("attSession", session);
		model.addAttribute("attList", as.getLessonAtts(lesson));
		return "attendance/session";
	}

	// ===== 교수: 출석부 · 수정 =====

	// 출석부 - 과정 전체 학생 × 수업 회차를 한 표로 (LRN-20)
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/book/{courseId}")
	public String book(Model model, @PathVariable("courseId") Long courseId, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Course course = as.getCourse(courseId);
		checkCourseOwner(course, principal);

		List<Lesson> lessons = as.getCourseLessons(course);

		model.addAttribute("course", course);
		model.addAttribute("lessons", lessons);
		model.addAttribute("rows", as.getBook(course, lessons));
		return "attendance/book";
	}

	// 출석 달력 - 한 달 달력에 날짜별로 진행한 수업과 출석 현황 (?month=2026-10, 없으면 이번 달)
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/calendar/{courseId}")
	public String calendar(Model model, @PathVariable("courseId") Long courseId,
			@RequestParam(value = "month", defaultValue = "") String month, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Course course = as.getCourse(courseId);
		checkCourseOwner(course, principal);

		// month 값이 이상하면 이번 달로
		YearMonth ym;
		try {
			ym = month.isEmpty() ? YearMonth.now() : YearMonth.parse(month);
		} catch (DateTimeParseException e) {
			ym = YearMonth.now();
		}

		List<Lesson> lessons = as.getCourseLessons(course);
		List<AttBookRow> rows = as.getBook(course, lessons);
		Map<Long, LocalDate> dates = as.getLessonDates(course);

		model.addAttribute("course", course);
		model.addAttribute("monthLabel", ym.getYear() + "년 " + ym.getMonthValue() + "월");
		model.addAttribute("prevMonth", ym.minusMonths(1).toString());   // 예: 2026-09
		model.addAttribute("nextMonth", ym.plusMonths(1).toString());
		model.addAttribute("days", as.getMonth(lessons, rows, dates, ym));
		model.addAttribute("studentCount", rows.size());
		// 날짜가 없는(아직 진행 안 한) 수업 - 달력에는 안 나오므로 따로 보여줌
		model.addAttribute("undated", lessons.stream()
				.filter(l -> !dates.containsKey(l.getLessonId()))
				.toList());
		return "attendance/calendar";
	}

	// 수업 하루치 출석 관리 - 그 수업의 학생 전체 상태를 보고 수정 (달력에서 수업을 누르면 옴)
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/day/{lessonId}")
	public String day(Model model, @PathVariable("lessonId") Long lessonId, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Lesson lesson = as.getLesson(lessonId);
		checkOwner(lesson, principal);

		Course course = lesson.getCourse();
		List<AttBookRow> rows = as.getBook(course, List.of(lesson));   // 이 수업 한 칸만 있는 출석부

		model.addAttribute("lesson", lesson);
		model.addAttribute("course", course);
		model.addAttribute("date", as.getLessonDates(course).get(lessonId));   // 없으면 null (아직 진행 안 함)
		model.addAttribute("rows", rows);
		model.addAttribute("present", rows.stream().mapToInt(AttBookRow::getPresent).sum());
		model.addAttribute("late", rows.stream().mapToInt(AttBookRow::getLate).sum());
		model.addAttribute("excused", rows.stream().mapToInt(AttBookRow::getExcused).sum());
		model.addAttribute("absent", rows.stream().mapToInt(AttBookRow::getAbsent).sum());
		return "attendance/day";
	}

	// 출석 상태 수정 (LRN-21) - 학생 1명 · 수업 1회차의 상태를 바꿈
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/modify/{lessonId}")
	public String modify(@PathVariable("lessonId") Long lessonId,
			@RequestParam("userId") String userId,
			@RequestParam("status") String status,
			@RequestParam(value = "back", defaultValue = "") String back,
			Principal principal, RedirectAttributes ra) {
		if (principal == null) return LOGIN_REDIRECT;

		Lesson lesson = as.getLesson(lessonId);
		checkOwner(lesson, principal);

		// 출석 Y / 지각 L / 공결 E / 결석 N 만 허용
		if (!status.matches("[YLEN]")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "출석 상태가 올바르지 않습니다.");
		}

		Users student = us.getUser(userId);
		if (!"S".equals(student.getUserRole())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학생만 출석을 수정할 수 있습니다.");
		}

		as.modify(lesson, student, status);

		ra.addFlashAttribute("bookMsg", student.getUserName() + " 학생의 [" + lesson.getLessonTitle()
				+ "] 출석을 '" + statusLabel(status) + "'(으)로 변경했습니다.");

		// 수정한 학생 줄로 돌아가기 (#u-학번) - 하루 화면에서 왔으면 그 화면으로, 아니면 출석부로
		String target = "day".equals(back)
				? "/attendance/day/" + lessonId
				: "/attendance/book/" + lesson.getCourse().getCourseId();
		return "redirect:" + target + "#u-" + userId;
	}

	// ===== 학생 =====

	// 코드 입력 - 맞으면 출석, 틀리면 다시 (5회까지)
	@PreAuthorize("hasRole('STUDENT')")
	@PostMapping("/check/{lessonId}")
	public String check(@PathVariable("lessonId") Long lessonId,
			@RequestParam(value = "code", defaultValue = "") String code,
			Principal principal, RedirectAttributes ra) {
		if (principal == null) return LOGIN_REDIRECT;

		// 6자리 숫자가 아니면 틀린 횟수에 넣지 않고 안내만
		code = code.trim();
		if (!code.matches("\\d{6}")) {
			return flash(ra, false, "6자리 숫자를 입력해 주세요.");
		}

		Lesson lesson = as.getLesson(lessonId);
		Users student = us.getUser(principal.getName());

		AttResult result;
		try {
			result = as.check(lesson, student, code);
		} catch (DataIntegrityViolationException e) {
			// 같은 요청이 동시에 두 번 들어와 먼저 처리된 경우
			result = AttResult.of(AttResult.Status.ALREADY);
		}

		return switch (result.status()) {
			case SUCCESS     -> flash(ra, true, "출석이 완료되었습니다.");
			case WRONG       -> flash(ra, false, "틀렸습니다. 다시 입력해 주세요. (남은 기회 " + result.remaining() + "회)");
			case LOCKED      -> flash(ra, false, AttService.MAX_FAIL + "회 모두 틀려서 이번 수업은 더 이상 입력할 수 없습니다.");
			case CLOSED      -> flash(ra, false, "출석 시간이 지났습니다.");
			case ALREADY     -> flash(ra, true, "이미 출석했습니다.");
			case PROCESSED   -> flash(ra, false, "교수님이 이미 출석 상태를 처리한 수업입니다.");
			case NOT_STARTED -> flash(ra, false, "아직 출석이 시작되지 않은 수업입니다.");
		};
	}

	// 결과 메시지를 담아 출석 화면으로 (새로고침해도 다시 제출되지 않게 redirect)
	private String flash(RedirectAttributes ra, boolean ok, String message) {
		ra.addFlashAttribute("resultOk", ok);
		ra.addFlashAttribute("resultMsg", message);
		return "redirect:/attendance/list";
	}

	// 상태 코드 → 글자
	private String statusLabel(String status) {
		return switch (status) {
			case "Y" -> "출석";
			case "L" -> "지각";
			case "E" -> "공결";
			default  -> "결석";
		};
	}

	// ===== 권한 확인 =====

	// 로그인한 사람이 교수인지 (ROLE_TEACHER)
	private boolean isTeacher(Principal principal) {
		if (principal instanceof Authentication auth) {
			return auth.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
		}
		return false;
	}

	// 이 수업을 맡은 교수가 아니면 403
	private void checkOwner(Lesson lesson, Principal principal) {
		Users user = us.getUser(principal.getName());
		if (!isTeacher(principal) || !as.isOwner(lesson, user)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 수업을 맡은 교수만 사용할 수 있습니다.");
		}
	}

	// 이 과정을 맡은 교수가 아니면 403
	private void checkCourseOwner(Course course, Principal principal) {
		Users user = us.getUser(principal.getName());
		if (!isTeacher(principal) || !as.isCourseOwner(course, user)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 과정을 맡은 교수만 볼 수 있습니다.");
		}
	}
}
