package com.hitech.lms.domain.qna;

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

/** 수업 Q&A Controller  —  URL prefix: /qna
 *  담당: 홍승훈
 *  화면: templates/qna/ (list.html, detail.html, form.html)
 *  질문 = 학생, 답변 = 교수 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/qna")
public class QnaController {

	private final QnaService qs;

	private final UserService us;

	// Q&A 목록 
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue = "0") int page,
			                        @RequestParam(value="kw", defaultValue = "") String kw) {


		return "qna/list";
	}

	// Q&A 상세 질문 + 답변 같이 보여줌
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id, AnswerForm answerForm) {


		return "qna/detail";
	}

	// 질문 작성 화면 - 학생
	@PreAuthorize("hasRole('STUDENT')")
	@GetMapping("/create")
	public String qnaCreate(QnaForm qnaForm) {
		return "qna/form";
	}

	// 질문 작성 완료 - 학생
	@PreAuthorize("hasRole('STUDENT')")
	@PostMapping("/create")
	public String qnaCreate(@Valid QnaForm qnaForm, BindingResult bindingResult, Principal principal) {


		return "redirect:/qna/list"; // TODO: "redirect:/qna/detail/" + qna.getQnaId()
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

	// 질문 삭제 - 작성자
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String qnaDelete(Principal principal, @PathVariable("id") Long id) {


		return "redirect:/qna/list";
	}

	// 답변 등록  - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/answer/create/{id}")
	public String answerCreate(Model model, @PathVariable("id") Long id,
			@Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal) {


		return "redirect:/qna/detail/" + id;
	}

	// 답변 수정  - 교수
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

}
