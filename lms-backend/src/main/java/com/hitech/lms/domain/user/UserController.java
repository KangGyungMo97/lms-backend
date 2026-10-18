package com.hitech.lms.domain.user;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hitech.lms.global.error.DataNotFoundException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private static final String PW_SESSION = "pwFind";

	private final UserService us;
	private final PasswordFindService pfs;

	// 로그인 화면 (POST /user/login 처리는 스프링 시큐리티가 해줌)
	// 이미 로그인한 상태로 들어오면 대시보드로 보냄
	@GetMapping("/login")
	public String login(Principal principal) {
		if (principal != null) {
			return "redirect:/dashboard";
		}
		return "login";
	}

	// ===== 아이디(학번/사번) 찾기 =====

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

	// ===== 비밀번호 찾기 (1.학번/사번 → 2.휴대폰 인증 → 3.새 비밀번호) =====

	// 화면: 세션의 진행 단계에 맞춰 find_pw.html 이 단계별로 보여줌
	@GetMapping("/find-pw")
	public String findPwForm(HttpSession session, Model model) {
		PwFindState state = getState(session);
		model.addAttribute("state", state != null ? state : new PwFindState());
		return "find_pw";
	}

	// 1단계: 학번/사번 입력 -> 다음
	@PostMapping("/find-pw/id")
	public String findPwId(@RequestParam("userId") String userId,
	                       HttpSession session, RedirectAttributes ra) {
		try {
			session.setAttribute(PW_SESSION, pfs.start(userId));
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/user/find-pw";
	}

	// 2단계: 인증번호 받기
	@PostMapping("/find-pw/send")
	public String findPwSend(@RequestParam("phone") String phone,
	                         HttpSession session, RedirectAttributes ra) {
		PwFindState state = getState(session);
		if (notStep(state, 2)) return "redirect:/user/find-pw";

		try {
			pfs.sendCode(state, phone);
			session.setAttribute(PW_SESSION, state);
			ra.addFlashAttribute("msg", "인증번호를 발송했습니다.");
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		ra.addFlashAttribute("phone", phone);    // 입력값 유지
		return "redirect:/user/find-pw";
	}

	// 2단계: 인증번호 확인
	@PostMapping("/find-pw/verify")
	public String findPwVerify(@RequestParam("code") String code,
	                           @RequestParam(value = "phone", defaultValue = "") String phone,
	                           HttpSession session, RedirectAttributes ra) {
		PwFindState state = getState(session);
		if (notStep(state, 2)) return "redirect:/user/find-pw";

		try {
			pfs.verifyCode(state, code);
			ra.addFlashAttribute("msg", "인증이 완료되었습니다. 다음 버튼을 눌러 주세요.");
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		session.setAttribute(PW_SESSION, state);
		ra.addFlashAttribute("phone", phone);
		return "redirect:/user/find-pw";
	}

	// 2단계: 이전 -> 처음으로
	@PostMapping("/find-pw/prev")
	public String findPwPrev(HttpSession session) {
		session.removeAttribute(PW_SESSION);
		return "redirect:/user/find-pw";
	}

	// 2단계: 다음 -> 3단계 (인증 완료해야 이동 가능)
	@PostMapping("/find-pw/next")
	public String findPwNext(HttpSession session, RedirectAttributes ra) {
		PwFindState state = getState(session);
		if (notStep(state, 2)) return "redirect:/user/find-pw";

		if (state.isVerified()) {
			state.setStep(3);
			session.setAttribute(PW_SESSION, state);
		} else {
			ra.addFlashAttribute("error", "휴대폰 인증을 먼저 완료해 주세요.");
		}
		return "redirect:/user/find-pw";
	}

	// 3단계: 비밀번호 변경
	@PostMapping("/find-pw/reset")
	public String findPwReset(@RequestParam("newPassword") String newPassword,
	                          @RequestParam("newPasswordConfirm") String newPasswordConfirm,
	                          HttpSession session, RedirectAttributes ra) {
		PwFindState state = getState(session);
		if (notStep(state, 3)) return "redirect:/user/find-pw";

		try {
			pfs.resetPassword(state, newPassword, newPasswordConfirm);
			session.removeAttribute(PW_SESSION);
			return "redirect:/user/login?pwChanged";
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/user/find-pw";
		}
	}

	// 취소 -> 진행 상태 지우고 로그인 화면으로
	@GetMapping("/find-pw/cancel")
	public String findPwCancel(HttpSession session) {
		session.removeAttribute(PW_SESSION);
		return "redirect:/user/login";
	}

	private PwFindState getState(HttpSession session) {
		return (PwFindState) session.getAttribute(PW_SESSION);
	}

	// 세션이 없거나 현재 단계가 아니면 처음 화면으로 되돌리기 위한 체크
	private boolean notStep(PwFindState state, int step) {
		return state == null || state.getStep() != step;
	}

	// 로그아웃 GET /user/logout 도 스프링 시큐리티가 처리 (SecurityConfig 참고)
	// 회원가입 없음 -> 계정은 관리자가 등록 (us.create)
}