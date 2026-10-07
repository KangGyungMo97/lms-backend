package com.hitech.lms.domain.main;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DashboardController {
	
	// 각 role 에 따라 redirect 해줌.
	@GetMapping("/dashboard") 
	public String dashboard(Authentication auth) {
		if (hasRole(auth, "ROLE_ADMIN"))   return "redirect:/dashboard/admin";
		if (hasRole(auth, "ROLE_TEACHER")) return "redirect:/dashboard/teacher";
		return "redirect:/dashboard/student";
	}
	
	// Principal principal = Authentication auth 같은 개념임.
	@GetMapping("/dashboard/student")
	public String student(Authentication auth, Model model) {
		model.addAttribute("userId", auth.getName());   // 로그인한 학번
		return "dashboard/student";
	}

	@GetMapping("/dashboard/teacher")
	public String teacher(Authentication auth, Model model) {
		model.addAttribute("userId", auth.getName());
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
