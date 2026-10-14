package com.hitech.lms.domain.admin;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;

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
	public String userList(Model model,
			@RequestParam(value="page",   defaultValue = "0")  int page,
			@RequestParam(value="kw",     defaultValue = "")   String kw,
			@RequestParam(value="role",   defaultValue = "")   String role,
			@RequestParam(value="deptId", defaultValue = "")   String deptId,
			@RequestParam(value="status", defaultValue = "")   String status) {

		model.addAttribute("paging", as.getUserList(page, kw, role, deptId, status));
		model.addAttribute("stats", as.getUserStats());
		model.addAttribute("deptList", as.getDeptList());

		// 검색 조건 유지용
		model.addAttribute("kw", kw);
		model.addAttribute("role", role);
		model.addAttribute("deptId", deptId);
		model.addAttribute("status", status);

		return "admin/user_list";
	}
	
	// 회원 상세
		@GetMapping("/user/{id}")
		public String userDetail(@PathVariable("id") String id, Model model) {
			Users user = us.getUser(id);
			model.addAttribute("user", user);
			model.addAttribute("logs", as.getStatusLogs(user));
			return "admin/user_detail";
		}

		// 회원 상태 변경 (정상 / 잠금 해제 / 차단 / 탈퇴)
		@PostMapping("/user/status/{id}")
		public String userStatus(@PathVariable("id") String id,
				@RequestParam("status") String status,
				@RequestParam("reason") String reason,
				@RequestParam(value = "blockStart", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate blockStart,
				@RequestParam(value = "blockEnd",   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate blockEnd,
				Principal principal, RedirectAttributes ra) {

			try {
				as.changeStatus(us.getUser(id), status, reason, blockStart, blockEnd, principal.getName());
				ra.addFlashAttribute("msg", "계정 상태가 변경되었습니다.");
			} catch (IllegalArgumentException e) {
				ra.addFlashAttribute("error", e.getMessage());
			}
			return "redirect:/admin/user/" + id;
		}

		// 임시 비밀번호 발급
		@PostMapping("/user/{id}/temp-password")
		public String tempPassword(@PathVariable("id") String id, RedirectAttributes ra) {
			us.resetPassword(id);
			ra.addFlashAttribute("msg", "비밀번호가 사용자의 생년월일(8자리)로 초기화되었습니다.");
			return "redirect:/admin/user/" + id;
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
