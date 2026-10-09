package com.hitech.lms.domain.course;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	public String list(Model model, @RequestParam(value="page", defaultValue = "0") int page) {


		return "lesson/list";
	}

	// 수업 상세 - 전체
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id) {


		return "lesson/detail";
	}

}
