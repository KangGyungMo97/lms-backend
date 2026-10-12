package com.hitech.lms.domain.main;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	// 예전 주소 /login 으로 와도 새 로그인 화면으로 보내기 (팀원 코드의 redirect:/login 대응)
	@GetMapping("/login")
	public String login() {
		return "redirect:/user/login";
	}

	// 메인 화면
	@GetMapping("/")
	public String main() {
		return "main";
	}

	// 기관소개 - 과정 소개 (ABT-01)
	@PreAuthorize("isAuthenticated()")
	@GetMapping({"/about", "/about/course"})
	public String aboutCourse() {
		return "about/course";
	}

}