package com.hitech.lms.domain.course;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService cs;
	private final SubjectService ss;

	// ---------- 공통 라벨 (교시 시각은 실제 교시표로 교체) ----------
	// ---------- 공통 라벨 (교시 시각은 실제 교시표로 교체) ----------
	private static final Map<Integer, String> TARGET = new LinkedHashMap<>();
	private static final Map<Integer, String> START = new LinkedHashMap<>();
	private static final Map<Integer, String> END = new LinkedHashMap<>();
	private static final Map<String, String> WEEKDAY = new LinkedHashMap<>();

	static {
		for (int i = 1; i <= 4; i++) TARGET.put(i, i + "학년");

		String[] s = {"09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00"};
		String[] e = {"09:50","10:50","11:50","12:50","13:50","14:50","15:50","16:50","17:50"};
		for (int i = 0; i < s.length; i++) { START.put(i + 1, s[i]); END.put(i + 1, e[i]); }

		String[] w = {"월","화","수","목","금","토","일"};
		for (int i = 0; i < w.length; i++) WEEKDAY.put(String.valueOf(i + 1), w[i]);
	}

	private void addLabels(Model model) {
		model.addAttribute("targetLabels", TARGET);
		model.addAttribute("startLabels", START);
		model.addAttribute("endLabels", END);
		model.addAttribute("weekdayLabels", WEEKDAY);
	}

	private void addFormOptions(Model model) {
		model.addAttribute("subjects", ss.getList());   // SubjectService의 전체 조회 메서드
		addLabels(model);
	}

	private void validateRange(CourseForm f, BindingResult br) {
		if (f.getCourseStartDate() != null && f.getCourseEndDate() != null
				&& f.getCourseEndDate().isBefore(f.getCourseStartDate())) {
			br.rejectValue("courseEndDate", "invalid", "종료일은 시작일 이후여야 합니다.");
		}

		List<CourseForm.TimeSlot> ts = f.getTimes();
		for (int i = 0; i < ts.size(); i++) {
			CourseForm.TimeSlot a = ts.get(i);
			if (a.getWeekday() == null || a.getStartTime() == null || a.getEndTime() == null) continue;

			if (a.getEndTime() < a.getStartTime()) {
				br.rejectValue("times[" + i + "].endTime", "invalid", "종료 교시는 시작 교시 이후여야 합니다.");
				continue;
			}
			for (int j = 0; j < i; j++) {
				CourseForm.TimeSlot b = ts.get(j);
				if (b.getWeekday() == null || b.getStartTime() == null || b.getEndTime() == null) continue;
				if (a.getWeekday().equals(b.getWeekday())
						&& a.getStartTime() <= b.getEndTime() && b.getStartTime() <= a.getEndTime()) {
					br.rejectValue("times[" + i + "].startTime", "overlap", "같은 요일에 시간이 겹칩니다.");
					break;
				}
			}
		}
	}

	// ---------- 목록 / 상세 ----------
	@GetMapping("/list")
	public String list(@RequestParam(name = "page", defaultValue = "0") int page,
	                   Principal principal, Model model) {
		Page<Course> paging = cs.getTeacherCourses(principal.getName(), page);
		model.addAttribute("paging", paging);
		model.addAttribute("isTeacher", true);   // 임시: 실제로는 권한/역할로 판단
		addLabels(model);
		return "course/list";
	}

	@GetMapping("/detail/{courseId}")
	public String detail(@PathVariable("courseId") Long courseId, Model model) {
		model.addAttribute("course", cs.getCourse(courseId));
		addLabels(model);
		return "course/detail";
	}

	// ---------- 등록 ----------
	@GetMapping("/create")
	@PreAuthorize("hasRole('TEACHER')")
	public String createForm(CourseForm courseForm, Model model) {
		courseForm.getTimes().add(new CourseForm.TimeSlot());
		addFormOptions(model);
		return "course/form";
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('TEACHER')")
	public String create(@Valid CourseForm courseForm, BindingResult br,
	                     Principal principal, Model model) {
		validateRange(courseForm, br);
		if (br.hasErrors()) {
			if (courseForm.getTimes().isEmpty()) courseForm.getTimes().add(new CourseForm.TimeSlot());
			addFormOptions(model);
			return "course/form";
		}
		Long id = cs.create(courseForm, principal.getName());
		return "redirect:/course/detail/" + id;
	}

	// ---------- 수정 ----------
	@GetMapping("/modify/{courseId}")
	@PreAuthorize("hasRole('TEACHER')")
	public String modifyForm(@PathVariable("courseId") Long courseId,
	                         CourseForm courseForm, Principal principal, Model model) {
		Course c = cs.getOwnedCourse(courseId, principal.getName());
		courseForm.setCourseTitle(c.getCourseTitle());
		courseForm.setSubjectId(c.getSubject().getSubjectId());
		courseForm.setCourseTarget(c.getCourseTarget());
		courseForm.setCourseStartDate(c.getCourseStartDate());
		courseForm.setCourseEndDate(c.getCourseEndDate());
		for (CourseTime t : c.getTimes()) {
			CourseForm.TimeSlot s = new CourseForm.TimeSlot();
			s.setWeekday(t.getWeekday());
			s.setStartTime(t.getStartTime());
			s.setEndTime(t.getEndTime());
			courseForm.getTimes().add(s);
		}
		if (courseForm.getTimes().isEmpty()) {   // 예전 방식으로 등록된 강의
			CourseForm.TimeSlot s = new CourseForm.TimeSlot();
			s.setWeekday(c.getCourseWeekday());
			s.setStartTime(c.getCourseStartTime());
			s.setEndTime(c.getCourseEndTime());
			courseForm.getTimes().add(s);
		}
		courseForm.setCourseCriteria(c.getCourseCriteria());
		model.addAttribute("courseId", courseId);
		addFormOptions(model);
		return "course/form";
	}

	@PostMapping("/modify/{courseId}")
	@PreAuthorize("hasRole('TEACHER')")
	public String modify(@PathVariable("courseId") Long courseId,
	                     @Valid CourseForm courseForm, BindingResult br,
	                     Principal principal, Model model) {
		validateRange(courseForm, br);
		if (br.hasErrors()) {
			if (courseForm.getTimes().isEmpty()) courseForm.getTimes().add(new CourseForm.TimeSlot());
			model.addAttribute("courseId", courseId);
			addFormOptions(model);
			return "course/form";
		}
		cs.update(courseId, courseForm, principal.getName());
		return "redirect:/course/detail/" + courseId;
	}

	// ---------- 삭제 (POST만 허용) ----------
	@PostMapping("/delete/{courseId}")
	@PreAuthorize("hasRole('TEACHER')")
	public String delete(@PathVariable("courseId") Long courseId, Principal principal) {
		cs.delete(courseId, principal.getName());
		return "redirect:/course/list";
	}
}