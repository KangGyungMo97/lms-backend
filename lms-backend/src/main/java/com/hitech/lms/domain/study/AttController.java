package com.hitech.lms.domain.study;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

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

import com.hitech.lms.domain.course.ClassClock;
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

	// 출석 첫 화면
	//   학생: 오늘의 수업(코드 입력) + 내 출석 기록
	//   교수: 오늘의 수업(수업 시작 · 코드) + 과정 선택(select) → 그 과정의 차시별 출석 기록
	@PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
	@GetMapping("/list")
	public String list(Model model, Principal principal,
			@RequestParam(value = "courseId", required = false) Long courseId) {
		if (principal == null) return LOGIN_REDIRECT;

		Users user = us.getUser(principal.getName());

		// 교수
		if (isTeacher(principal)) {
			List<Course> courses = as.getTeacherCourses(user);
			model.addAttribute("todayList", as.getTodayClasses(courses, null));
			model.addAttribute("courseList", courses);

			// 선택한 과정 (없거나 내 과정이 아니면 첫 번째 과정)
			Course selected = courses.stream()
					.filter(c -> c.getCourseId().equals(courseId))
					.findFirst()
					.orElse(courses.isEmpty() ? null : courses.get(0));
			model.addAttribute("selected", selected);

			if (selected != null) {
				List<LessonAttRow> rows = as.getLessonRows(selected);
				model.addAttribute("lessonRows", rows);
				// 요약: 전체 차시 / 진행한 차시 / 평균 출석률
				List<LessonAttRow> done = rows.stream().filter(r -> !"NONE".equals(r.getState())).toList();
				model.addAttribute("totalLessons", rows.size());
				model.addAttribute("doneLessons", done.size());
				model.addAttribute("avgRate", done.isEmpty() ? null
						: (int) Math.round(done.stream().mapToInt(LessonAttRow::getRate).average().orElse(0)));
				model.addAttribute("rosterCount", as.getRoster(selected).size());
			}
			return "attendance/teacher";
		}

		// 학생
		model.addAttribute("todayList", as.getTodayClasses(as.getStudentCourses(user), user));
		model.addAttribute("myAtts", as.getMyAtts(user));
		return "attendance/student";
	}

	// ===== 교수: 수업 시작 =====

	// 수업 시작 - 오늘 수업 시간에만. 다음 차시의 출석이 열리고 출석 현황 화면으로
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/start/{courseId}")
	public String start(@PathVariable("courseId") Long courseId,
			@RequestParam(value = "back", defaultValue = "") String back,
			Principal principal, RedirectAttributes ra) {
		if (principal == null) return LOGIN_REDIRECT;

		Course course = as.getCourse(courseId);
		Users user = us.getUser(principal.getName());
		if (!as.isCourseOwner(course, user)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 과정을 맡은 교수만 사용할 수 있습니다.");
		}

		AttResult result;
		try {
			result = as.start(course);
		} catch (DataIntegrityViolationException e) {
			// 버튼을 동시에 두 번 눌러 이미 만들어진 경우 → 만들어진 세션으로
			result = as.findTodaySession(course)
					.map(s -> AttResult.of(AttResult.Status.STARTED, s.getLesson().getLessonId()))
					.orElse(AttResult.of(AttResult.Status.NOT_STARTED));
		}

		if (result.status() == AttResult.Status.STARTED) {
			return "redirect:/attendance/session/" + result.lessonId();
		}
		String msg = switch (result.status()) {
			case NO_CLASS  -> "오늘은 이 과정의 수업이 없습니다.";
			case TOO_EARLY -> "수업 시작 " + AttService.ENTER_BEFORE_MINUTES + "분 전부터 시작할 수 있습니다.";
			case CLOSED    -> "오늘 수업 시간이 끝났습니다.";
			case NO_LESSON -> "진행할 차시가 없습니다. 먼저 차시를 등록해 주세요.";
			default        -> "수업을 시작하지 못했습니다.";
		};
		ra.addFlashAttribute("flashMsg", msg);
		ra.addFlashAttribute("flashOk", false);
		return "redirect:" + backUrl(back) + ("dashboard".equals(back) ? "" : "?courseId=" + courseId);
	}

	// 출석 현황 - 이 차시에 들어온 학생 (이 수업을 맡은 교수)
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
		model.addAttribute("rosterCount", as.getRoster(lesson.getCourse()).size());
		model.addAttribute("lateMinutes", AttService.LATE_MINUTES);
		// 이 시각 이후 입력은 지각 (수업 시작 시각과 교수가 시작한 시각 중 늦은 쪽 + 10분)
		LocalDateTime base = session.getOpenedAt();
		var slot = ClassClock.on(lesson.getCourse(), base.toLocalDate());
		if (slot.isPresent() && slot.get().start().isAfter(base)) base = slot.get().start();
		model.addAttribute("lateAt", base.plusMinutes(AttService.LATE_MINUTES));
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

	// ===== 학생: 출석 코드 입력 =====

	// 코드 입력 - 맞으면 출석 / 지각, 틀리면 남은 기회 안내 (5회 틀리면 잠김)
	@PreAuthorize("hasRole('STUDENT')")
	@PostMapping("/check/{courseId}")
	public String check(@PathVariable("courseId") Long courseId,
			@RequestParam(value = "code", defaultValue = "") String code,
			@RequestParam(value = "back", defaultValue = "") String back,
			Principal principal, RedirectAttributes ra) {
		if (principal == null) return LOGIN_REDIRECT;

		// 6자리 숫자가 아니면 틀린 횟수에 넣지 않고 안내만
		code = code.trim();
		if (!code.matches("\\d{6}")) {
			ra.addFlashAttribute("flashMsg", "출석 코드는 6자리 숫자입니다.");
			ra.addFlashAttribute("flashOk", false);
			return "redirect:" + backUrl(back);
		}

		Course course = as.getCourse(courseId);
		Users student = us.getUser(principal.getName());

		AttResult result;
		try {
			result = as.check(course, student, code);
		} catch (DataIntegrityViolationException e) {
			// 같은 요청이 동시에 두 번 들어와 먼저 처리된 경우
			result = AttResult.of(AttResult.Status.ALREADY);
		}

		String msg = switch (result.status()) {
			case PRESENT     -> "출석 처리되었습니다.";
			case LATE        -> "지각 처리되었습니다. (수업 시작 " + AttService.LATE_MINUTES + "분 이후 입력)";
			case WRONG       -> "코드가 틀렸습니다. 다시 입력해 주세요. (남은 기회 " + result.remaining() + "회)";
			case LOCKED      -> AttService.MAX_FAIL + "회 모두 틀려서 이번 수업은 더 이상 입력할 수 없습니다. 교수님께 말씀드려 주세요.";
			case ALREADY     -> "이미 출석한 수업입니다.";
			case PROCESSED   -> "교수님이 출석 상태를 처리한 수업입니다.";
			case NOT_STARTED -> "교수님이 아직 수업을 시작하지 않았습니다.";
			case NO_CLASS    -> "오늘은 이 과정의 수업이 없습니다.";
			case TOO_EARLY   -> "수업 시작 " + AttService.ENTER_BEFORE_MINUTES + "분 전부터 입력할 수 있습니다.";
			case CLOSED      -> "오늘 수업 시간이 끝났습니다.";
			case NOT_MINE    -> "수강 대상이 아닌 과정입니다.";
			default          -> "출석하지 못했습니다.";
		};
		boolean ok = result.status() == AttResult.Status.PRESENT || result.status() == AttResult.Status.LATE
				|| result.status() == AttResult.Status.ALREADY;
		ra.addFlashAttribute("flashMsg", msg);
		ra.addFlashAttribute("flashOk", ok);
		return "redirect:" + backUrl(back);
	}

	// 돌아갈 화면 - 대시보드에서 왔으면 대시보드, 아니면 출석 화면
	private String backUrl(String back) {
		return "dashboard".equals(back) ? "/dashboard" : "/attendance/list";
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
