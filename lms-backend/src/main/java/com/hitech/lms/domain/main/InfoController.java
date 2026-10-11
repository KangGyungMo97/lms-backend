package com.hitech.lms.domain.main;

import java.security.Principal;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hitech.lms.domain.admin.AdminPost;
import com.hitech.lms.domain.file.FileService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/info")
public class InfoController {

	// 시스템 공지 조회는 InfoService 사용 (AdminService 는 관리자 기능 담당이라 건드리지 않음)
	private final InfoService is;

	private final FileService fs;

	// ※ SecurityConfig 에 @EnableMethodSecurity 가 아직 없어서 @PreAuthorize 가 동작하지 않음
	//    → 로그인 안 한 경우(principal == null) 를 메서드 안에서 직접 막아 둠
	private static final String LOGIN_REDIRECT = "redirect:/login";

	// 정보마당 메뉴 (/info) → 시스템 공지 목록으로 이동
	@GetMapping({"", "/"})
	public String info() {
		return "redirect:/info/notice/list";
	}

	// 시스템 공지 목록 (INF-01)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/notice/list")
	public String noticeList(Model model, @RequestParam(value="page", defaultValue = "0") int page,
			                              @RequestParam(value="kw", defaultValue = "") String kw,
			                              Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Page<AdminPost> paging = is.getNoticeList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		return "info/notice_list";
	}

	// 시스템 공지 상세 (INF-02)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/notice/detail/{id}")
	public String noticeDetail(Model model, @PathVariable("id") Long id, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		AdminPost notice = is.getNotice(id);
		is.increaseViewCount(notice);
		model.addAttribute("notice", notice);
		model.addAttribute("prev", is.getPrev(id));   // 이전 글 (없으면 null)
		model.addAttribute("next", is.getNext(id));   // 다음 글 (없으면 null)
		return "info/notice_detail";
	}

	// 첨부파일 내려받기 (INF-02) - P3 (안 해도 OK), FileService 완성 후 구현
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/notice/download/{fileId}")
	public ResponseEntity<Resource> noticeDownload(@PathVariable("fileId") Long fileId) {

		return null;
	}

}
