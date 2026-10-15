package com.hitech.lms.domain.main;

import java.security.Principal;

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

	// 첫 주소 (http://localhost:8080/)
	//   로그인 안 했으면 → 로그인 화면 / 로그인했으면 → 대시보드 (/dashboard 가 권한별로 다시 나눠 줌)
	//   ※ 예전 main.html 은 지우지 않고 남겨 둠 (안 쓰면 나중에 삭제)
	@GetMapping("/")
	public String main(Principal principal) {
		if (principal == null) {
			return "redirect:/user/login";
		}
		return "redirect:/dashboard";
	}

	// 기관소개 - 과정 소개 (ABT-01)
	@PreAuthorize("isAuthenticated()")
	@GetMapping({"/about", "/about/course"})
	public String aboutCourse() {
		return "about/course";
	}

	// 학과 안내 - 교육과정 안내 (목록) - DB 연결 전까지 화면만 (정적 HTML)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/about/curriculum")
	public String curriculumList() {
		return "about/curriculum_list";
	}

	// 학과 안내 - 교육과정 상세 - DB 연결 전까지 화면만 (정적 HTML)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/about/curriculum/detail")
	public String curriculumDetail() {
		return "about/curriculum_detail";
	}

}