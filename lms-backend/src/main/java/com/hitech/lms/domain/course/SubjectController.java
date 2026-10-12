package com.hitech.lms.domain.course;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hitech.lms.domain.user.DeptRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/subject")
public class SubjectController {

	private final SubjectService ss;
	private final DeptRepository dr;

	// 과목 목록
	@GetMapping("/list")
	public String list(Model model,
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "kw", defaultValue = "") String kw) {
		Page<Subject> paging = ss.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		return "subject/list";
	}


	// 과목 상세
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") String id) {
		model.addAttribute("subject", ss.getSubject(id));
		return "subject/detail";
	}

	// 과목 등록 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/create")
	public String subjectCreate(SubjectForm subjectForm, Model model) {
		model.addAttribute("depts", dr.findAll());
		return "subject/form";
	}

	// 과목 등록 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/create")
	public String subjectCreate(@Valid SubjectForm subjectForm, BindingResult bindingResult,
			Principal principal, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("depts", dr.findAll());
			return "subject/form";
		}
		try {
			ss.create(subjectForm.getSubjectId(), subjectForm.getSubjectName(), subjectForm.getDeptId());
		} catch (IllegalArgumentException e) {
			bindingResult.rejectValue("subjectId", "duplicate", e.getMessage());
			model.addAttribute("depts", dr.findAll());
			return "subject/form";
		}
		return "redirect:/subject/list";
	}

	// 과목 수정 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/update/{id}")
	public String subjectUpdate(SubjectForm subjectForm, Principal principal,
		@PathVariable("id") String id, Model model) {
		Subject subject = ss.getSubject(id);
		subjectForm.setSubjectId(subject.getSubjectId());
		subjectForm.setSubjectName(subject.getSubjectName());
		subjectForm.setDeptId(subject.getDept().getDeptId());
		model.addAttribute("depts", dr.findAll());
		model.addAttribute("isUpdate", true);   // 템플릿에서 subjectId readonly 처리용
		return "subject/form";
	}

	// 과목 수정 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/update/{id}")
	public String subjectUpdate(@Valid SubjectForm subjectForm, BindingResult bindingResult,
		Principal principal, @PathVariable("id") String id, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("depts", dr.findAll());
			model.addAttribute("isUpdate", true);
			return "subject/form";
		}
		Subject subject = ss.getSubject(id);
		ss.modify(subject, subjectForm.getSubjectName(), subjectForm.getDeptId());
		return "redirect:/subject/detail/" + id;
	}

	// 과목 삭제 - 교수 (POST)
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/delete/{id}")
	public String subjectDelete(Principal principal, @PathVariable("id") String id) {
		Subject subject = ss.getSubject(id);
		ss.delete(subject);
		return "redirect:/subject/list";
	}

}
