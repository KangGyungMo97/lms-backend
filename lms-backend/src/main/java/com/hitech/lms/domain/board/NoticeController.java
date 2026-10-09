package com.hitech.lms.domain.board;

import java.security.Principal;

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

@RequiredArgsConstructor
@Controller
@RequestMapping("/notice")
public class NoticeController {

	private final NoticeService ns;

	private final UserService us;

	// 공지 목록 - 전체
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue = "0") int page,
			                        @RequestParam(value="kw", defaultValue = "") String kw) {


		return "notice/list";
	}

	// 공지 상세 - 전체 (조회수 +1)
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id) {


		return "notice/detail";
	}

	// 공지 작성 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/create")
	public String noticeCreate(NoticeForm noticeForm) {
		return "notice/form";
	}

	// 공지 작성 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/create")
	public String noticeCreate(@Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal) {

		return "redirect:/notice/list";
	}

	// 공지 수정 화면 - 교수 (기존 값 채움)
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/update/{id}")
	public String noticeUpdate(NoticeForm noticeForm, Principal principal, @PathVariable("id") Long id) {


		return "notice/form";
	}

	// 공지 수정 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/update/{id}")
	public String noticeUpdate(@Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Long id) {

		return "redirect:/notice/detail/" + id;
	}

	// 공지 삭제 - 교수 & 관리자 (POST)
	@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
	@PostMapping("/delete/{id}")
	public String noticeDelete(Principal principal, @PathVariable("id") Long id) {


		return "redirect:/notice/list";
	}

}
