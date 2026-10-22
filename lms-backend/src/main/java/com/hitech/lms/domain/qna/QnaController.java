package com.hitech.lms.domain.qna;

import java.security.Principal;

import org.springframework.data.domain.Page;
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

import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/qna")
public class QnaController {

	private final QnaService qs;

	private final UserService us;

	// Q&A 목록 (CLS-03)	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue = "0") int page,
			                        @RequestParam(value="kw", defaultValue = "") String kw,
			                        Principal principal) {

		Page<QnaBoard> paging = qs.getList(page, kw);

		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("isStudent", hasRole(principal, "ROLE_STUDENT"));   // 질문하기 버튼

		return "qna/list";
	}

	// Q&A 상세 = 질문 + 답변 같이 보여줌
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id, AnswerForm answerForm, Principal principal) {

		QnaBoard qna = qs.getQna(id);

		setDetailModel(model, qna, principal);

		return "qna/detail";
	}

	// 질문 작성 화면  - 학생
	@PreAuthorize("hasRole('STUDENT')")
	@GetMapping("/create")
	public String qnaCreate(Model model, QnaForm qnaForm) {

		model.addAttribute("lessonList", qs.getLessonList());   // 수업 선택 목록

		return "qna/form";
	}

	// 질문 작성 완료 - 학생
	@PreAuthorize("hasRole('STUDENT')")
	@PostMapping("/create")
	public String qnaCreate(Model model, @Valid QnaForm qnaForm, BindingResult bindingResult, Principal principal) {

		// 1. 유효성 검사 오류 있으면 → 작성 화면 다시 (수업 목록 다시 담기)
		if (bindingResult.hasErrors()) {
			model.addAttribute("lessonList", qs.getLessonList());
			return "qna/form";
		}

		// 2. 로그인한 학생으로 질문 저장
		Users user = us.getUser(principal.getName());
		QnaBoard qna = qs.create(qnaForm.getTitle(), qnaForm.getContent(), qnaForm.getLessonId(), user);

		// 3. 저장한 질문 상세로
		return "redirect:/qna/detail/" + qna.getQnaId();
	}

	// 질문 수정 화면 - 작성자 
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String qnaModify(QnaForm qnaForm, Principal principal, @PathVariable("id") Long id) {


		return "qna/form";
	}

	// 질문 수정 완료 - 작성자 
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String qnaModify(@Valid QnaForm qnaForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Long id) {


		return "redirect:/qna/detail/" + id;
	}

	// 질문 삭제 - 작성자 (P2)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String qnaDelete(Principal principal, @PathVariable("id") Long id) {


		return "redirect:/qna/list";
	}

	// 답변 등록- 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/answer/create/{id}")
	public String answerCreate(Model model, @PathVariable("id") Long id,
			@Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal) {

		// 1. 질문 · 로그인한 교수 찾기
		QnaBoard qna = qs.getQna(id);
		Users user = us.getUser(principal.getName());

		// 2. 유효성 검사 오류 있으면 → 상세 화면 다시 
		if (bindingResult.hasErrors()) {
			setDetailModel(model, qna, principal);
			return "qna/detail";
		}

		// 3. 답변 저장 (질문 상태 → 답변완료)
		qs.createAnswer(qna, answerForm.getContent(), user);

		return "redirect:/qna/detail/" + id;
	}

	// 답변 수정 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/answer/modify/{answerId}")
	public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
			@PathVariable("answerId") Long answerId, Principal principal) {


		return "redirect:/qna/list"; // TODO: "redirect:/qna/detail/" + answer.getQnaBoard().getQnaId()
	}

	// 답변 삭제 - 교수 
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/answer/delete/{answerId}")
	public String answerDelete(Principal principal, @PathVariable("answerId") Long answerId) {


		return "redirect:/qna/list"; // TODO: "redirect:/qna/detail/" + answer.getQnaBoard().getQnaId()
	}

	// ===== 공통 =====

	// 상세 화면에 필요한 값 담기 (상세 보기 · 답변 실패 시 같이 씀)
	private void setDetailModel(Model model, QnaBoard qna, Principal principal) {
		model.addAttribute("qna", qna);
		model.addAttribute("answerList", qs.getAnswerList(qna));
		model.addAttribute("isTeacher", hasRole(principal, "ROLE_TEACHER"));   // 답변 폼
	}

	// 로그인한 사람의 권한 확인 (ROLE_STUDENT / ROLE_TEACHER / ROLE_ADMIN)
	private boolean hasRole(Principal principal, String role) {
		if (principal instanceof Authentication auth) {
			return auth.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals(role));
		}
		return false;
	}

}
