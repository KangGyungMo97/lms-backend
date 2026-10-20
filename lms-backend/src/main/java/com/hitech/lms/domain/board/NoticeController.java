package com.hitech.lms.domain.board;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.course.CourseService;
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/notice")
public class NoticeController {

	private final NoticeService ns;
	private final UserService us;
	private final CourseService cs;

	// 공지 목록 - 전체
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "kw", defaultValue = "") String kw) {
		Page<Notice> paging = ns.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		return "notice/list";
	}

	// 공지 상세 - 전체 (조회수 +1)
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id,
		HttpServletRequest request, HttpServletResponse response) {
		Notice notice = ns.getNotice(id, request, response);  // 쿠키로 조회수 처리
		model.addAttribute("notice", notice);
		return "notice/detail";
	}

	// 공지 작성 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/create")
	public String noticeCreate(NoticeForm noticeForm, Model model, Principal principal) {
		addCourses(model, principal);
		return "notice/form";
	}

	// 공지 작성 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/create")
	public String noticeCreate(@Valid NoticeForm noticeForm, BindingResult bindingResult,
		Model model, Principal principal) {
		if (bindingResult.hasErrors()) {
			addCourses(model, principal);
			return "notice/form";
		}
		Users writer = us.getUser(principal.getName());
		Course course = cs.getOwnedCourse(noticeForm.getCourseId(), principal.getName());  // 본인 강의만 허용
		ns.create(noticeForm.getTitle(), noticeForm.getContent(), writer, course);
		return "redirect:/notice/list";
	}

	// 공지 수정 화면 - 교수 (기존 값 채움)
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/update/{id}")
	public String noticeUpdate(NoticeForm noticeForm, Model model, Principal principal,
		@PathVariable("id") Long id) {
		Notice notice = ns.getNotice(id);                 // 조회수 안 올라가는 쪽
		checkWriter(notice, principal);
		noticeForm.setTitle(notice.getNoticeTitle());
		noticeForm.setContent(notice.getNoticeContent());
		if (notice.getCourse() != null) {
			noticeForm.setCourseId(notice.getCourse().getCourseId());
		}
		model.addAttribute("mode", "modify");
		addCourses(model, principal);
		return "notice/form";
	}

	// 공지 수정 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/update/{id}")
	public String noticeUpdate(@Valid NoticeForm noticeForm, BindingResult bindingResult,
		Model model, Principal principal, @PathVariable("id") Long id) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "modify");
			addCourses(model, principal);
			return "notice/form";
		}
		Notice notice = ns.getNotice(id);
		checkWriter(notice, principal);
		Course course = cs.getOwnedCourse(noticeForm.getCourseId(), principal.getName());
		ns.modify(notice, noticeForm.getTitle(), noticeForm.getContent(), course);
		return "redirect:/notice/detail/" + id;
	}

	// 공지 삭제 - 교수 & 관리자 (POST)
	@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
	@PostMapping("/delete/{id}")
	public String noticeDelete(Principal principal, HttpServletRequest request, @PathVariable("id") Long id) {
	    Notice notice = ns.getNotice(id);
	    
	    // 현재 로그인한 사용자가 관리자인지 확인
	    boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
	    
	    // 관리자가 아닐 때만 작성자 본인인지 체크
	    if (!isAdmin) {
	        checkWriter(notice, principal);
	    }
	    
	    ns.delete(notice);
	    return "redirect:/notice/list";
	}

	// 드롭다운에 보여 줄 교수 본인 강의
	private void addCourses(Model model, Principal principal) {
		model.addAttribute("courses", cs.getTeacherCourseList(principal.getName()));
	}

	private void checkWriter(Notice notice, Principal principal) {
		if (!notice.getWriter().getUserId().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정/삭제 권한이 없습니다.");
		}
	}
}