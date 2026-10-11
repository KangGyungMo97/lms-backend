package com.hitech.lms.domain.board;

import java.security.Principal;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.hitech.lms.domain.file.FileService;
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class BoardController {

	private final BoardService bs;

	private final UserService us;

	private final FileService fs;

	private static final String LOGIN_REDIRECT = "redirect:/login";

	// 게시글 목록
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue = "0") int page, @RequestParam(value="kw", defaultValue = "") String kw,
			Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Page<Board> paging = bs.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		return "board/list";
	}

	// 게시글 상세
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id, CommentForm commentForm, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Board board = bs.getBoard(id);
		bs.increaseViewCount(board);
		model.addAttribute("board", board);
		model.addAttribute("isWriter", isWriter(board, principal));            // 수정 버튼
		model.addAttribute("canDelete", isWriter(board, principal) || isAdmin(principal)); // 삭제 버튼
		return "board/detail";
	}

	// 첨부파일 내려받기
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/download/{fileId}")
	public ResponseEntity<Resource> download(@PathVariable("fileId") Long fileId) {

		return null;
	}

	// 작성 화면
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
	public String boardCreate(BoardForm boardForm, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;
		return "board/form";
	}

	// 작성 완료
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public String boardCreate(@Valid BoardForm boardForm, BindingResult bindingResult, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;
		if (bindingResult.hasErrors()) {
			return "board/form";
		}
		Users writer = bs.getUser(principal.getName());
		bs.create(boardForm.getTitle(), boardForm.getContent(), writer);
		return "redirect:/board/list";
	}

	// 수정 화면
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String boardModify(Model model, BoardForm boardForm, Principal principal, @PathVariable("id") Long id) {
		if (principal == null) return LOGIN_REDIRECT;

		Board board = bs.getBoard(id);
		if (!isWriter(board, principal)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
		}
		boardForm.setTitle(board.getBoardTitle());
		boardForm.setContent(board.getBoardContent());
		model.addAttribute("mode", "modify");   // 화면 제목 · 버튼을 "수정" 으로
		return "board/form";
	}

	// 수정 완료
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String boardModify(Model model, @Valid BoardForm boardForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Long id) {
		if (principal == null) return LOGIN_REDIRECT;
		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "modify");
			return "board/form";
		}
		Board board = bs.getBoard(id);
		if (!isWriter(board, principal)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
		}
		bs.modify(board, boardForm.getTitle(), boardForm.getContent());
		return "redirect:/board/detail/" + id;
	}

	// 게시글 삭제 (작성자 또는 관리자)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String boardDelete(Principal principal, @PathVariable("id") Long id) {
		if (principal == null) return LOGIN_REDIRECT;

		Board board = bs.getBoard(id);
		if (!isWriter(board, principal) && !isAdmin(principal)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
		}
		bs.delete(board);
		return "redirect:/board/list";
	}

	// ===== 댓글 =====

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

	// ===== 권한 확인 =====

	// 로그인한 사람이 글 작성자인지
	private boolean isWriter(Board board, Principal principal) {
		return principal != null && board.getWriter().getUserId().equals(principal.getName());
	}

	// 로그인한 사람이 관리자인지 (ROLE_ADMIN)
	private boolean isAdmin(Principal principal) {
		if (principal instanceof Authentication auth) {
			return auth.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		}
		return false;
	}

}
