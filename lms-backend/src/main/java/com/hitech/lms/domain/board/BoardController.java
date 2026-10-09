package com.hitech.lms.domain.board;

import java.security.Principal;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hitech.lms.domain.file.FileService;
import com.hitech.lms.domain.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class BoardController {

	private final BoardService bs;

	private final UserService us;

	private final FileService fs;

	// 게시글 목록
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue = "0") int page, @RequestParam(value="kw", defaultValue = "") String kw) {

		return "board/list";
	}

	// 게시글 상세
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id, CommentForm commentForm) {

		return "board/detail";
	}

	// 첨부파일 내려받기
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/download/{fileId}")
	public ResponseEntity<Resource> download(@PathVariable("fileId") Long fileId) {

		return null;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
	public String boardCreate(BoardForm boardForm) {
		return "board/form";
	}

	// 작성 완료
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public String boardCreate(@Valid BoardForm boardForm, BindingResult bindingResult, Principal principal) {

		return "redirect:/board/list";
	}

	// 수정 화면
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String boardModify(BoardForm boardForm, Principal principal, @PathVariable("id") Long id) {

		return "board/form";
	}

	// 수정 완료
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String boardModify(@Valid BoardForm boardForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Long id) {

		return "redirect:/board/detail/" + id;
	}

	// 게시글 삭제 
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String boardDelete(Principal principal, @PathVariable("id") Long id) {

		return "redirect:/board/list";
	}

	// 댓글 작성 
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/comment/create/{id}")
	public String commentCreate(Model model, @PathVariable("id") Long id,
			@Valid CommentForm commentForm, BindingResult bindingResult, Principal principal) {


		return "redirect:/board/detail/" + id;
	}

	// 댓글 수정 
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/comment/modify/{commentId}")
	public String commentModify(@Valid CommentForm commentForm, BindingResult bindingResult,
			@PathVariable("commentId") Long commentId, Principal principal) {

		return "redirect:/board/list"; // TODO: "redirect:/board/detail/" + 게시글 id
	}

	// 댓글 삭제
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/comment/delete/{commentId}")
	public String commentDelete(Principal principal, @PathVariable("commentId") Long commentId) {

		return "redirect:/board/list"; // TODO: "redirect:/board/detail/" + 게시글 id
	}

}
