package com.hitech.lms.domain.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mypage")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MyPageController {

	private final MyPageService ms;

	// 마이페이지 (조회 + 수정 폼)
	@GetMapping("")
	public String view(@ModelAttribute("profileForm") ProfileForm profileForm,
	                   @ModelAttribute("passwordForm") PasswordForm passwordForm,
	                   Authentication auth, Model model) {
		Users u = ms.getUser(auth.getName());
		profileForm.setUserTel(u.getUserTel());
		profileForm.setUserEmail(u.getUserEmail());
		fill(model, u);
		return "mypage/view";
	}

	// 연락처 수정
	@PostMapping("/profile")
	public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm profileForm,
	                            BindingResult br, Authentication auth,
	                            Model model, RedirectAttributes ra) {
		if (!br.hasErrors()) {
			try {
				ms.updateProfile(auth.getName(), profileForm);
				ra.addFlashAttribute("flashMsg", "연락처가 수정되었습니다.");
				ra.addFlashAttribute("flashOk", true);
				return "redirect:/mypage";
			} catch (IllegalArgumentException e) {
				br.rejectValue("userEmail", "duplicate", e.getMessage());
			}
		}
		model.addAttribute("passwordForm", new PasswordForm());
		fill(model, ms.getUser(auth.getName()));
		return "mypage/view";
	}

	// 비밀번호 변경
	@PostMapping("/password")
	public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm passwordForm,
	                             BindingResult br, Authentication auth,
	                             Model model, RedirectAttributes ra) {

		if (!br.hasErrors() && !passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())) {
			br.rejectValue("newPasswordConfirm", "mismatch", "새 비밀번호가 일치하지 않습니다.");
		}
		if (!br.hasErrors()) {
			try {
				ms.changePassword(auth.getName(), passwordForm);
				ra.addFlashAttribute("flashMsg", "비밀번호가 변경되었습니다.");
				ra.addFlashAttribute("flashOk", true);
				return "redirect:/mypage";
			} catch (IllegalArgumentException e) {
				br.reject("changeFail", e.getMessage());
			}
		}

		Users u = ms.getUser(auth.getName());
		ProfileForm pf = new ProfileForm();
		pf.setUserTel(u.getUserTel());
		pf.setUserEmail(u.getUserEmail());
		model.addAttribute("profileForm", pf);
		fill(model, u);
		return "mypage/view";
	}

	// 화면에 보여 줄 값
	private void fill(Model model, Users u) {
		model.addAttribute("me", u);
		model.addAttribute("roleLabel",
				"S".equals(u.getUserRole()) ? "학생" : ("P".equals(u.getUserRole()) ? "교수" : "관리자"));

		String b = u.getUserBday();   // 20070315 -> 2007-03-15
		model.addAttribute("bdayText",
				b == null ? "-" : (b.length() == 8 ? b.substring(0, 4) + "-" + b.substring(4, 6) + "-" + b.substring(6) : b));
	}
}