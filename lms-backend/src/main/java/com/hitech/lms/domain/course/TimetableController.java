package com.hitech.lms.domain.course;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/timetable")
public class TimetableController {

	private final TimetableService ts;

	// 시간표 조회 - 학생 & 교수 (URL: /timetable), 조회 전용
	@PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
	@GetMapping("")
	public String list(@RequestParam(name = "semester", required = false) String semester,
	                   @RequestParam(name = "scope", defaultValue = "mine") String scope,
	                   @RequestParam(name = "grade", required = false) Integer grade,
	                   Authentication auth, Model model) {

		boolean teacher = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
		boolean all = teacher && "all".equals(scope);
		String sem = ts.normalizeSemester(semester);

		// 목록에 없는 값이 들어오면 비어 있는 것(전체)으로 처리
		Integer target = (grade != null && CourseController.TARGET.containsKey(grade)) ? grade : null;

		// 학생은 '전체'가 없으므로 값이 비어 있으면 첫 번째 대상(1학년)으로
		if (!teacher && target == null) {
			target = CourseController.TARGET.keySet().iterator().next();
		}

		model.addAttribute("grid", ts.build(auth.getName(), teacher, all, sem, target));
		model.addAttribute("semester", sem);
		model.addAttribute("semesters", ts.getSemesterOptions());
		model.addAttribute("scope", all ? "all" : "mine");
		model.addAttribute("isTeacher", teacher);
		model.addAttribute("grade", target);                         // 선택값 유지용

		model.addAttribute("targetLabels", CourseController.TARGET); // 대상 드롭다운 옵션
		model.addAttribute("startLabels", CourseController.START);
		model.addAttribute("endLabels", CourseController.END);
		model.addAttribute("weekdayLabels", CourseController.WEEKDAY);
		return "timetable/list";
	}
}