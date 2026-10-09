package com.hitech.lms.domain.course;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hitech.lms.domain.user.UserService;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Controller
@RequestMapping("/timetable")
public class TimetableController {

	private final TimetableService ts;

	private final UserService us;

	// 시간표 조회 - 학생 & 교수  (URL: /timetable)
	@PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
	@GetMapping("")
	public String list(Model model, Principal principal) {


		return "timetable/list";
	}

}
