package com.hitech.lms.domain.main;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.qna.QnaBoard;
import com.hitech.lms.domain.study.AttService;
import com.hitech.lms.domain.study.TodayClass;
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService ds;

	private final AttService as;

	private final UserService us;

	// 각 role 에 따라 redirect 해줌.
	@GetMapping("/dashboard")
	public String dashboard(Authentication auth) {
		if (hasRole(auth, "ROLE_ADMIN"))   return "redirect:/dashboard/admin";
		if (hasRole(auth, "ROLE_TEACHER")) return "redirect:/dashboard/teacher";
		return "redirect:/dashboard/student";
	}

	// 학생 대시보드 - 요약 숫자 · 오늘의 수업 · 수강 중 과목 · 마감 임박 과제 · 공지사항
	// Principal principal = Authentication auth 같은 개념임.
	@GetMapping("/dashboard/student")
	public String student(Authentication auth, Model model) {
		Users user = us.getUser(auth.getName());
		List<Course> courses = ds.getStudentCourses(user);
		List<DashHomeworkRow> homeworks = ds.getStudentHomeworks(user, courses);
		List<TodayClass> todayList = as.getTodayClasses(courses, user);

		model.addAttribute("user", user);
		model.addAttribute("semester", ds.semesterLabel());

		// 요약 숫자 4개
		model.addAttribute("courseCount", courses.size());
		model.addAttribute("todayCount", todayList.size());
		model.addAttribute("unsubmitted", ds.countUnsubmitted(homeworks));
		model.addAttribute("attendRate", ds.getMonthAttendRate(user, courses));   // 진행한 수업이 없으면 null

		model.addAttribute("todayList", todayList);
		model.addAttribute("courseRows", ds.getStudentCourseRows(courses));
		model.addAttribute("homeworkRows", homeworks.stream().limit(DashboardService.LIMIT).toList());
		model.addAttribute("notices", ds.getNotices());
		return "dashboard/student";
	}

	// 교수 대시보드 - 요약 숫자 · 오늘의 수업 · 담당 과목 · 채점 대기 과제 · 미답변 Q&A
	@GetMapping("/dashboard/teacher")
	public String teacher(Authentication auth, Model model) {
		Users user = us.getUser(auth.getName());
		List<Course> courses = ds.getTeacherCourses(user);
		Map<Long, Integer> rosterMap = ds.getRosterMap(courses);
		List<DashHomeworkRow> homeworks = ds.getTeacherHomeworks(courses, rosterMap);
		List<TodayClass> todayList = as.getTodayClasses(courses, null);
		List<QnaBoard> qnaList = ds.getWaitingQna(user);

		model.addAttribute("user", user);
		model.addAttribute("semester", ds.semesterLabel());

		// 요약 숫자 4개
		model.addAttribute("courseCount", courses.size());
		model.addAttribute("todayCount", todayList.size());
		model.addAttribute("ungraded", ds.countUngraded(homeworks));
		model.addAttribute("waitingQna", qnaList.size());

		model.addAttribute("todayList", todayList);
		model.addAttribute("courseRows", ds.getTeacherCourseRows(courses, rosterMap));
		model.addAttribute("homeworkRows", homeworks.stream().limit(DashboardService.LIMIT).toList());
		model.addAttribute("qnaList", qnaList.stream().limit(DashboardService.LIMIT).toList());
		return "dashboard/teacher";
	}

	@GetMapping("/dashboard/admin")
	public String admin(Authentication auth, Model model) {
		model.addAttribute("userId", auth.getName());
		return "dashboard/admin";
	}

	private boolean hasRole(Authentication auth, String role) {
		return auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(role));
	}
}
