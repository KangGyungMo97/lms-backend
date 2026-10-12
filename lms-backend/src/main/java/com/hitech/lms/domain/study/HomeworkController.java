package com.hitech.lms.domain.study;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.server.ResponseStatusException;

import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/homework")
public class HomeworkController {

	private final HomeworkService hs;

	private final UserService us;

	// ※ 이제 SecurityConfig 에 @EnableMethodSecurity 가 있어서 @PreAuthorize 가 동작함
	//    아래 principal == null 체크는 혹시 몰라 남겨 둔 것 (지워도 됨)
	private static final String LOGIN_REDIRECT = "redirect:/login";

	// ===== 과제 =====

	// 과제 목록 (LRN-01, LRN-12) - 학생: 제출 여부까지 / 교수: 내가 낸 과제
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String list(Model model, Principal principal,
			@RequestParam(value="page", defaultValue = "0") int page) {
		if (principal == null) return LOGIN_REDIRECT;

		Users user = us.getUser(principal.getName());
		Page<Homework> paging = hs.getList(page, user);

		model.addAttribute("paging", paging);
		model.addAttribute("isTeacher", isTeacher(principal));            // 과제 등록 버튼 · 상태 칸
		if (!isTeacher(principal)) {
			model.addAttribute("submitStatus", hs.getMySubmitStatus(user)); // { 과제번호 : 제출상태 }
		}
		model.addAttribute("today", LocalDate.now());                      // 마감 여부 표시용
		return "homework/list";
	}

	// 과제 상세 (LRN-02~04, LRN-14) - 학생이면 내 제출 내역도 같이
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id, SubmitForm submitForm, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		Homework homework = hs.getHomework(id);
		Users user = us.getUser(principal.getName());

		setDetailModel(model, homework, user, principal);
		return "homework/detail";
	}

	// 과제 상세 화면에 필요한 값 담기 (상세 보기 · 제출 실패 시 같이 씀)
	private void setDetailModel(Model model, Homework homework, Users user, Principal principal) {
		model.addAttribute("homework", homework);
		model.addAttribute("isTeacher", isTeacher(principal));
		model.addAttribute("isOwner", isTeacher(principal) && hs.isOwner(homework, user)); // 수정 · 삭제 · 제출현황 버튼
		if (!isTeacher(principal)) {
			model.addAttribute("mySubmit", hs.getMySubmit(homework, user));  // 아직 안 냈으면 null
		}
		// 마감 여부 · 남은 날짜 (D-n)
		LocalDate today = LocalDate.now();
		model.addAttribute("closed", homework.getHomeworkEndDate().isBefore(today));
		model.addAttribute("daysLeft", ChronoUnit.DAYS.between(today, homework.getHomeworkEndDate()));
	}

	// 과제 등록 화면  - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/create")
	public String homeworkCreate(Model model, HomeworkForm homeworkForm, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;
		checkTeacher(principal);

		Users teacher = us.getUser(principal.getName());
		model.addAttribute("lessonList", hs.getMyLessons(teacher));   // 수업 선택 목록

		// 처음 열 때 기간 기본값: 오늘 ~ 7일 뒤
		homeworkForm.setStartDate(LocalDate.now());
		homeworkForm.setEndDate(LocalDate.now().plusDays(7));
		return "homework/form";
	}

	// 과제 등록 완료 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/create")
	public String homeworkCreate(Model model, @Valid HomeworkForm homeworkForm, BindingResult bindingResult, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;
		checkTeacher(principal);

		Users teacher = us.getUser(principal.getName());

		// 마감일이 시작일보다 빠르면 오류
		if (homeworkForm.getStartDate() != null && homeworkForm.getEndDate() != null
				&& homeworkForm.getEndDate().isBefore(homeworkForm.getStartDate())) {
			bindingResult.rejectValue("endDate", "dateOrder", "마감일은 시작일과 같거나 그 이후여야 합니다.");
		}

		// 내가 담당하는 수업에만 과제를 낼 수 있음
		if (homeworkForm.getLessonId() != null
				&& hs.getMyLessons(teacher).stream().noneMatch(l -> l.getLessonId().equals(homeworkForm.getLessonId()))) {
			bindingResult.rejectValue("lessonId", "notMyLesson", "담당하는 수업만 선택할 수 있습니다.");
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute("lessonList", hs.getMyLessons(teacher));   // 다시 그릴 때 수업 목록 필요
			return "homework/form";
		}

		hs.create(homeworkForm.getLessonId(), homeworkForm.getTitle(), homeworkForm.getContent(),
				homeworkForm.getStartDate(), homeworkForm.getEndDate());
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
		if (principal == null) return LOGIN_REDIRECT;

		// 1. 과제 · 로그인한 학생 찾기
		Homework homework = hs.getHomework(id);
		Users student = us.getUser(principal.getName());

		// 2. 마감 지났으면 / 이미 냈으면 제출 불가
		if (homework.getHomeworkEndDate().isBefore(LocalDate.now())) {
			bindingResult.reject("closed", "마감된 과제는 제출할 수 없습니다.");
		}
		if (hs.getMySubmit(homework, student) != null) {
			bindingResult.reject("already", "이미 제출한 과제입니다.");
		}

		// 3. 오류 있으면 상세 화면 다시 (입력한 내용 유지)
		if (bindingResult.hasErrors()) {
			setDetailModel(model, homework, student, principal);
			return "homework/detail";
		}

		// 4. 저장
		hs.createSubmit(homework, submitForm.getContent(), student);

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
	public String submitList(Model model, @PathVariable("id") Long id, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		// 1. 과제 찾고, 내가 낸 과제인지 확인
		Homework homework = hs.getHomework(id);
		checkOwner(homework, principal);

		// 2. 제출 목록 담기
		model.addAttribute("homework", homework);
		model.addAttribute("submitList", hs.getSubmitList(homework));

		return "homework/submit_list";
	}

	// 제출물 보기 · 채점 화면 - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/submit/detail/{submitId}")
	public String submitDetail(Model model, @PathVariable("submitId") Long submitId, GradeForm gradeForm, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		// 1. 제출물 찾고, 내가 낸 과제의 제출물인지 확인
		Submit submit = hs.getSubmit(submitId);
		checkOwner(submit.getHomework(), principal);

		// 2. 이미 채점했으면 기존 점수 · 피드백 채워서 보여주기 (다시 채점 가능)
		if ("R".equals(submit.getSubmitIsDone())) {
			gradeForm.setScore(submit.getSubmitScore());
			gradeForm.setFeedback(submit.getSubmitFeedback());
		}

		model.addAttribute("submit", submit);
		return "homework/submit_detail";
	}

	// 채점 · 피드백 저장  - 교수
	@PreAuthorize("hasRole('TEACHER')")
	@PostMapping("/submit/grade/{submitId}")
	public String submitGrade(Model model, @PathVariable("submitId") Long submitId,
			@Valid GradeForm gradeForm, BindingResult bindingResult, Principal principal) {
		if (principal == null) return LOGIN_REDIRECT;

		// 1. 제출물 찾고, 내가 낸 과제의 제출물인지 확인
		Submit submit = hs.getSubmit(submitId);
		checkOwner(submit.getHomework(), principal);

		// 2. 점수 범위 오류 등 있으면 채점 화면 다시
		if (bindingResult.hasErrors()) {
			model.addAttribute("submit", submit);
			return "homework/submit_detail";
		}

		// 3. 채점 저장 → 제출 현황으로
		hs.grade(submit, gradeForm.getScore(), gradeForm.getFeedback());

		return "redirect:/homework/submit/list/" + submit.getHomework().getHomeworkId();
	}

	// 전체 제출물 내려받기  - 교수, 압축 파일
	@PreAuthorize("hasRole('TEACHER')")
	@GetMapping("/submit/download/{id}")
	public ResponseEntity<Resource> submitDownload(@PathVariable("id") Long id) {



		return null;
	}

	// ===== 권한 확인 =====

	// 로그인한 사람이 교수인지 (ROLE_TEACHER)
	private boolean isTeacher(Principal principal) {
		if (principal instanceof Authentication auth) {
			return auth.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
		}
		return false;
	}

	// 교수가 아니면 403
	private void checkTeacher(Principal principal) {
		if (!isTeacher(principal)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 사용할 수 있습니다.");
		}
	}

	// 이 과제를 낸 교수가 아니면 403 (제출 현황 · 채점)
	private void checkOwner(Homework homework, Principal principal) {
		Users user = us.getUser(principal.getName());
		if (!isTeacher(principal) || !hs.isOwner(homework, user)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 과제를 낸 교수만 볼 수 있습니다.");
		}
	}

}
