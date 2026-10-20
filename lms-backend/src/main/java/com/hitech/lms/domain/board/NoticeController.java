package com.hitech.lms.domain.board;

import java.security.Principal;
import java.util.List;

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

import com.hitech.lms.domain.course.SubjectService;
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;     
import com.hitech.lms.domain.course.Subject; 

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
	private final SubjectService ss; 

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
		Notice notice = ns.getNotice(id, request, response);  
		model.addAttribute("notice", notice);
		return "notice/detail";
	}

	// 공지 작성 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/create")
	public String noticeCreate(NoticeForm noticeForm, Model model, Principal principal) {
		List<Subject> subjects = ss.getList();
		model.addAttribute("subjects", subjects);
		return "notice/form";
	}

	// 공지 작성 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/create")
	public String noticeCreate(@Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal, Model model) {
		if (bindingResult.hasErrors()) {
			List<Subject> subjects = ss.getList();
			model.addAttribute("subjects", subjects);
			return "notice/form";
		}
		Users writer = us.getUser(principal.getName());   
		
		ns.create(noticeForm.getSubjectId(), noticeForm.getTitle(), noticeForm.getContent(), writer);  
		return "redirect:/notice/list";
	}

	// 공지 수정 화면 - 교수 (기존 값 채움)
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/update/{id}")
	public String noticeUpdate(NoticeForm noticeForm, Principal principal, @PathVariable("id") Long id, Model model) {
		Notice notice = ns.getNotice(id);                 
		checkWriter(notice, principal);
		
		if (notice.getSubject() != null) {
			try {
				noticeForm.setSubjectId(notice.getSubject().getSubjectId());
			} catch (Exception e) {
				// 프록시 초기화 실패 예방 방어 코드
			}
		}
		
		List<Subject> subjects = ss.getList();
		model.addAttribute("subjects", subjects);
		model.addAttribute("mode", "modify"); 
		
		noticeForm.setTitle(notice.getNoticeTitle());
		noticeForm.setContent(notice.getNoticeContent());
		
		return "notice/form";
	}

	// 공지 수정 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/update/{id}")
	public String noticeUpdate(@Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal,
		@PathVariable("id") Long id, Model model) {
		if (bindingResult.hasErrors()) {
			List<Subject> subjects = ss.getList();
			model.addAttribute("subjects", subjects);
			model.addAttribute("mode", "modify");
			return "notice/form";
		}
		Notice notice = ns.getNotice(id);
		checkWriter(notice, principal);
		
		ns.modify(notice, noticeForm.getSubjectId(), noticeForm.getTitle(), noticeForm.getContent());
		return "redirect:/notice/detail/" + id;
	}

	// 공지 삭제 - 교수 & 관리자 (POST 방식 버그 수정 완료)
	@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
	@PostMapping("/delete/{id}")
	public String noticeDelete(Principal principal, HttpServletRequest request, @PathVariable("id") Long id) {
		Notice notice = ns.getNotice(id);
		
		// 로그인 한 사용자가 관리자(ADMIN) 권한을 가졌는지 검사
		boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
		
		// 관리자가 아닐 때만 본인 글 검증 수행 (관리자는 타인 글 삭제 가능)
		if (!isAdmin) {
			checkWriter(notice, principal);   
		}
		
		ns.delete(notice);
		return "redirect:/notice/list";
	}
	
	// 작성자 일치 검증 메서드
	private void checkWriter(Notice notice, Principal principal) {
		if (!notice.getWriter().getUserId().equals(principal.getName())) {  
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정/삭제 권한이 없습니다.");
		}
	}
}
