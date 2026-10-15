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
	                   @RequestParam(name = "grade", required = false) String grade, // 1. 학년 파라미터 수집
	                   Authentication auth, Model model) {

		boolean teacher = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
		boolean all = teacher && "all".equals(scope);
		String sem = ts.normalizeSemester(semester);

		// 2. ★핵심★ ts.build 인자에 수집한 grade를 함께 던져줍니다.
		// (기존 ts.build 메서드 시그니처에 String grade 파라미터를 추가하셔야 합니다)
		model.addAttribute("grid", ts.build(auth.getName(), teacher, all, sem, grade));
		
		model.addAttribute("semester", sem);
		model.addAttribute("semesters", ts.getSemesterOptions());
		model.addAttribute("scope", all ? "all" : "mine");
		model.addAttribute("isTeacher", teacher);
		
		// 3. 화면단 드롭다운에 선택 값 유지를 위해 model에 심어 전송
		model.addAttribute("grade", grade); 

		model.addAttribute("startLabels", CourseController.START);
		model.addAttribute("endLabels", CourseController.END);
		model.addAttribute("weekdayLabels", CourseController.WEEKDAY);
		return "timetable/list";
	}
}