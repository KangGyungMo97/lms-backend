package com.hitech.lms.domain.study;

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

import com.hitech.lms.domain.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/homework")
public class HomeworkController {

	private final HomeworkService hs;

	private final UserService us;

	// ===== 과제 =====

	// 과제 목록 
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String list(Model model, Principal principal,
			@RequestParam(value="page", defaultValue = "0") int page) {


		return "homework/list";
	}

	// 과제 상세 (LRN-02~04, LRN-14) - 학생이면 내 제출 내역도 같이
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id, SubmitForm submitForm, Principal principal) {


		return "homework/detail";
	}

	// 과제 등록 화면  - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/create")
	public String homeworkCreate(HomeworkForm homeworkForm) {
		return "homework/form";
	}

	// 과제 등록 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/create")
	public String homeworkCreate(@Valid HomeworkForm homeworkForm, BindingResult bindingResult, Principal principal) {



		return "redirect:/homework/list";
	}

	// 과제 수정 화면  - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/modify/{id}")
	public String homeworkModify(HomeworkForm homeworkForm, Principal principal, @PathVariable("id") Long id) {



		return "homework/form";
	}

	// 과제 수정 완료  - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/modify/{id}")
	public String homeworkModify(@Valid HomeworkForm homeworkForm, BindingResult bindingResult, Principal principal, @PathVariable("id") Long id) {



		return "redirect:/homework/detail/" + id;
	}

	// 과제 삭제  - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/delete/{id}")
	public String homeworkDelete(Principal principal, @PathVariable("id") Long id) {


		return "redirect:/homework/list";
	}

	// ===== 제출 (학생) =====

	// 과제 제출- 학생
	@PreAuthorize("hasRole('STUDENT')")
	@PostMapping("/submit/create/{id}")
	public String submitCreate(Model model, @PathVariable("id") Long id,
			@Valid SubmitForm submitForm, BindingResult bindingResult, Principal principal) {



		return "redirect:/homework/detail/" + id;
	}

	// 제출 수정 - 작성자
	@PreAuthorize("hasRole('STUDENT')")
	@PostMapping("/submit/modify/{submitId}")
	public String submitModify(@Valid SubmitForm submitForm, BindingResult bindingResult,
			@PathVariable("submitId") Long submitId, Principal principal) {



		return "redirect:/homework/list"; // TODO: "redirect:/homework/detail/" + submit.getHomework().getHomeworkId()
	}

	// 제출 삭제  - 작성자
	@PreAuthorize("hasRole('STUDENT')")
	@GetMapping("/submit/delete/{submitId}")
	public String submitDelete(Principal principal, @PathVariable("submitId") Long submitId) {



		return "redirect:/homework/list"; // TODO: "redirect:/homework/detail/" + submit.getHomework().getHomeworkId()
	}

	// ===== 제출 관리 (교수) =====

	// 제출 현황 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/submit/list/{id}")
	public String submitList(Model model, @PathVariable("id") Long id) {



		return "homework/submit_list";
	}

	// 제출물 보기 · 채점 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/submit/detail/{submitId}")
	public String submitDetail(Model model, @PathVariable("submitId") Long submitId, GradeForm gradeForm) {


		return "homework/submit_detail";
	}

	// 채점 · 피드백 저장  - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/submit/grade/{submitId}")
	public String submitGrade(Model model, @PathVariable("submitId") Long submitId,
			@Valid GradeForm gradeForm, BindingResult bindingResult) {



		return "redirect:/homework/list"; // TODO: "redirect:/homework/submit/list/" + submit.getHomework().getHomeworkId()
	}

	// 전체 제출물 내려받기  - 교수, 압축 파일
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/submit/download/{id}")
	public ResponseEntity<Resource> submitDownload(@PathVariable("id") Long id) {



		return null;
	}

}
