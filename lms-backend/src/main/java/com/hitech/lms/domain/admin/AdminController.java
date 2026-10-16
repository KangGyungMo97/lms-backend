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
	public String userCreate(Model model, UserCreateForm userCreateForm) {
		if (userCreateForm.getUserRole() == null) userCreateForm.setUserRole("S");   // 기본: 학생
		model.addAttribute("deptName", as.getFixedDeptName());
		return "admin/user_form";
	}

	// 계정 등록 완료 → 등록한 회원의 상세 화면으로
	@PostMapping("/user/create")
	public String userCreate(Model model, @Valid UserCreateForm userCreateForm, BindingResult bindingResult,
			RedirectAttributes ra) {
		if (!bindingResult.hasErrors()) {
			try {
				Users user = as.createUser(userCreateForm);
				ra.addFlashAttribute("msg", "계정이 등록되었습니다. 임시 비밀번호는 생년월일 8자리입니다. 처음 로그인 후 비밀번호를 바꾸도록 안내해 주세요.");
				return "redirect:/admin/user/" + user.getUserId();
			} catch (IllegalStateException e) {            // 학년 미선택 · 학번/이메일 중복
				String[] err = e.getMessage().split(":", 2);
				bindingResult.rejectValue(err[0], "duplicate", err[1]);
			} catch (IllegalArgumentException e) {         // 학과(D001)가 DB 에 없을 때
				bindingResult.reject("noDept", e.getMessage());
			}
		}
		model.addAttribute("deptName", as.getFixedDeptName());
		return "admin/user_form";
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

	// ===== 관리자 게시글 (시스템 공지) ===== (홍승훈)

	// 관리자 게시글 목록
	@GetMapping("/post/list")
	public String postList(Model model, @RequestParam(value="page", defaultValue = "0") int page,
			@RequestParam(value="kw", defaultValue = "") String kw) {
		model.addAttribute("paging", as.getPostList(page, kw));
		model.addAttribute("kw", kw);
		return "admin/post_list";
	}

	// 등록 화면
	@GetMapping("/post/create")
	public String postCreate(AdminPostForm adminPostForm) {
		return "admin/post_form";
	}

	// 등록 완료
	@PostMapping("/post/create")
	public String postCreate(@Valid AdminPostForm adminPostForm, BindingResult bindingResult,
			Principal principal, RedirectAttributes ra) {
		if (bindingResult.hasErrors()) {
			return "admin/post_form";
		}
		as.createPost(adminPostForm.getTitle(), adminPostForm.getContent(), principal.getName());
		ra.addFlashAttribute("msg", "게시글이 등록되었습니다. 정보마당 > 시스템 공지에 바로 보입니다.");
		return "redirect:/admin/post/list";
	}

	// 수정 화면
	@GetMapping("/post/modify/{id}")
	public String postModify(Model model, AdminPostForm adminPostForm, @PathVariable("id") Long id) {
		AdminPost post = as.getPost(id);
		adminPostForm.setTitle(post.getAdminPostTitle());
		adminPostForm.setContent(post.getAdminPostContent());
		model.addAttribute("mode", "modify");
		return "admin/post_form";
	}

	// 수정 완료
	@PostMapping("/post/modify/{id}")
	public String postModify(Model model, @Valid AdminPostForm adminPostForm, BindingResult bindingResult,
			@PathVariable("id") Long id, RedirectAttributes ra) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "modify");
			return "admin/post_form";
		}
		as.modifyPost(as.getPost(id), adminPostForm.getTitle(), adminPostForm.getContent());
		ra.addFlashAttribute("msg", "게시글이 수정되었습니다.");
		return "redirect:/admin/post/list";
	}

	// 삭제 (게시판 삭제와 같은 GET 방식)
	@GetMapping("/post/delete/{id}")
	public String postDelete(@PathVariable("id") Long id, RedirectAttributes ra) {
		as.deletePost(as.getPost(id));
		ra.addFlashAttribute("msg", "게시글이 삭제되었습니다.");
		return "redirect:/admin/post/list";
	}

	// ===== 학과 =====

	// 학과 관리
	@GetMapping("/dept/list")
	public String deptList(Model model) {

		return "admin/dept_list";
	}

}
