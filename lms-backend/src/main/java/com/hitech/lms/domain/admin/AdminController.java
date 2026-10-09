package com.hitech.lms.domain.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hitech.lms.domain.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

	private final AdminService as;

	private final UserService us;

	// ===== 회원 =====

	// 계정 등록 화면
	@GetMapping("/user/create")
	public String userCreate(UserCreateForm userCreateForm) {
		return "admin/user_form";
	}

	// 계정 등록 완료
	@PostMapping("/user/create")
	public String userCreate(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {

		return "redirect:/admin/user/list";
	}

	// 회원 목록
	@GetMapping("/user/list")
	public String userList(Model model, @RequestParam(value="page", defaultValue = "0") int page,
			                            @RequestParam(value="kw", defaultValue = "") String kw) {

		return "admin/user_list";
	}

	// 회원 상태 변경 (정상 / 잠금 / 차단)
	@PostMapping("/user/status/{id}")
	public String userStatus(@PathVariable("id") String id, @RequestParam("status") String status) {

		return "redirect:/admin/user/list";
	}

	// ===== 관리자 게시글 =====

	// 관리자 게시글 목록
	@GetMapping("/post/list")
	public String postList(Model model, @RequestParam(value="page", defaultValue = "0") int page, @RequestParam(value="kw", defaultValue = "") String kw) {

		return "admin/post_list";
	}

	// ===== 학과 =====

	// 학과 관리
	@GetMapping("/dept/list")
	public String deptList(Model model) {

		return "admin/dept_list";
	}

}
