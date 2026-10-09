package com.hitech.lms.domain.main;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	// 메인 화면
	@GetMapping("/")
	public String main() {
		return "main";
	}

	// 기관소개 - 과정 소개 (ABT-01)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/about/course")
	public String aboutCourse() {
		return "about/course";
	}

}
