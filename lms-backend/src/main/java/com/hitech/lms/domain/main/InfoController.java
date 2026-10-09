package com.hitech.lms.domain.main;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hitech.lms.domain.admin.AdminService;
import com.hitech.lms.domain.file.FileService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/info")
public class InfoController {

	private final AdminService as;

	private final FileService fs;

	// 시스템 공지 목록 (INF-01)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/notice/list")
	public String noticeList(Model model, @RequestParam(value="page", defaultValue = "0") int page,
			                              @RequestParam(value="kw", defaultValue = "") String kw) {


		return "info/notice_list";
	}

	// 시스템 공지 상세 (INF-02)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/notice/detail/{id}")
	public String noticeDetail(Model model, @PathVariable("id") Long id) {

		return "info/notice_detail";
	}

	// 첨부파일 내려받기 (INF-02)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/notice/download/{fileId}")
	public ResponseEntity<Resource> noticeDownload(@PathVariable("fileId") Long fileId) {



		return null;
	}

}
