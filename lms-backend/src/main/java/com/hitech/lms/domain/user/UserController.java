package com.hitech.lms.domain.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService us;

	// 로그인 화면 (POST /user/login 처리는 스프링 시큐리티가 해줌)
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/find-id")
	public String findIdForm() {
	    return "find_id";
	}
	
	@PostMapping("/find-id")
	public String findId(@RequestParam("userName") String userName,
			             @RequestParam("userBday") String userBday,
			             @RequestParam("userRole") String userRole,
			             Model model) {
	    try {
	        model.addAttribute("foundIds", us.findUserIds(userName, userBday, userRole));
	    } catch (DataNotFoundException | IllegalArgumentException e) {
	        model.addAttribute("error", e.getMessage());
	    }
	    return "find_id";
	}
	

	// 로그아웃 GET /user/logout 도 스프링 시큐리티가 처리 (SecurityConfig 참고)
	// 회원가입 없음 -> 계정은 관리자가 등록 (us.create)
}
