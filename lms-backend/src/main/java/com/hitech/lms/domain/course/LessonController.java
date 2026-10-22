package com.hitech.lms.domain.course;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/lesson")
public class LessonController {

	private final LessonService ls;

	// 수업 목록 - 전체
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {
		Page<Lesson> paging = ls.getList(page);
		model.addAttribute("paging", paging);
		return "lesson/list";
	}

	// 수업 상세 - 전체 (화면 없음, 기존 코드 유지)
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id) {
		Lesson lesson = ls.getLesson(id);
		model.addAttribute("lesson", lesson);
		return "lesson/detail";
	}

	// 수업 등록 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/create")
	public String create(Principal principal, Model model) {
		model.addAttribute("courses", ls.getMyCourses(principal.getName()));
		return "lesson/form";
	}

	// 수업 등록 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/create")
	public String create(@RequestParam(value = "courseId", required = false) Long courseId,
			@RequestParam(value = "lessonTitle", defaultValue = "") String lessonTitle,
			@RequestParam(value = "lessonContent", defaultValue = "") String lessonContent,
			Principal principal, Model model) {
		try {
			ls.create(courseId, lessonTitle, lessonContent, principal.getName());
		} catch (IllegalArgumentException e) {
			// 오류 메시지 + 입력값 유지한 채 폼 다시 표시
			model.addAttribute("error", e.getMessage());
			model.addAttribute("courses", ls.getMyCourses(principal.getName()));
			model.addAttribute("selectedCourseId", courseId);
			model.addAttribute("lessonTitle", lessonTitle);
			model.addAttribute("lessonContent", lessonContent);
			return "lesson/form";
		}
		return "redirect:/dashboard/teacher";
	}

	// [추가] 수업 수정 화면 - 교수 (본인 수업만)
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/update/{id}")
	public String update(@PathVariable("id") Long id, Principal principal, Model model) {
		Lesson lesson = ls.getMyLesson(id, principal.getName());
		model.addAttribute("isUpdate", true);
		model.addAttribute("lessonId", id);
		model.addAttribute("course", lesson.getCourse());
		model.addAttribute("lessonTitle", lesson.getLessonTitle());
		model.addAttribute("lessonContent", lesson.getLessonContent());
		return "lesson/form";
	}

	// [추가] 수업 수정 완료 - 교수 (본인 수업만)
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/update/{id}")
	public String update(@PathVariable("id") Long id,
			@RequestParam(value = "lessonTitle", defaultValue = "") String lessonTitle,
			@RequestParam(value = "lessonContent", defaultValue = "") String lessonContent,
			Principal principal, Model model) {
		try {
			ls.modify(id, lessonTitle, lessonContent, principal.getName());
		} catch (IllegalArgumentException e) {
			Lesson lesson = ls.getMyLesson(id, principal.getName());
			model.addAttribute("error", e.getMessage());
			model.addAttribute("isUpdate", true);
			model.addAttribute("lessonId", id);
			model.addAttribute("course", lesson.getCourse());
			model.addAttribute("lessonTitle", lessonTitle);
			model.addAttribute("lessonContent", lessonContent);
			return "lesson/form";
		}
		return "redirect:/lesson/list";
	}

	// [추가] 수업 삭제 - 교수 (본인 수업만, POST)
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable("id") Long id, Principal principal) {
		ls.delete(id, principal.getName());
		return "redirect:/lesson/list";
	}
}