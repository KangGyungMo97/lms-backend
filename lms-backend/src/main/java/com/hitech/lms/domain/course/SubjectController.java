package com.hitech.lms.domain.course;

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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/subject")
public class SubjectController {

	private final SubjectService ss;

	// 과목 목록
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue = "0") int page,
			                        @RequestParam(value="kw", defaultValue = "") String kw) {


		return "subject/list";
	}

	// 과목 상세
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") String id) {


		return "subject/detail";
	}

	// 과목 등록 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/create")
	public String subjectCreate(SubjectForm subjectForm) {
		return "subject/form";
	}

	// 과목 등록 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/create")
	public String subjectCreate(@Valid SubjectForm subjectForm, BindingResult bindingResult, Principal principal) {


		return "redirect:/subject/list";
	}

	// 과목 수정 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/update/{id}")
	public String subjectUpdate(SubjectForm subjectForm, Principal principal, @PathVariable("id") String id) {


		return "subject/form";
	}

	// 과목 수정 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/update/{id}")
	public String subjectUpdate(@Valid SubjectForm subjectForm, BindingResult bindingResult, Principal principal, @PathVariable("id") String id) {


		return "redirect:/subject/detail/" + id;
	}

	// 과목 삭제 - 교수 (POST)
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/delete/{id}")
	public String subjectDelete(Principal principal, @PathVariable("id") String id) {


		return "redirect:/subject/list";
	}

}
