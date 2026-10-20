package com.hitech.lms.domain.course;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.user.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

	private final CourseRepository cr;
	private final SubjectRepository sr;
	private final UsersRepository ur;

	public Page<Course> getTeacherCourses(String userId, int page) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("courseId").descending());
		return cr.findByTeacher_UserIdAndCourseIsUse(userId, "Y", pageable);
	}

	public Course getCourse(Long courseId) {
		return cr.findByCourseId(courseId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다. id=" + courseId));
	}

	@Transactional
	public Long create(CourseForm form, String teacherId) {
		Course c = new Course();
		apply(c, form);
		c.setTeacher(ur.findById(teacherId)
				.orElseThrow(() -> new IllegalArgumentException("교수 정보를 찾을 수 없습니다.")));
		return cr.save(c).getCourseId();
	}

	@Transactional
	public void update(Long courseId, CourseForm form, String teacherId) {
		Course c = getOwnedCourse(courseId, teacherId);
		apply(c, form);
	}

	@Transactional
	public void delete(Long courseId, String teacherId) {
		Course c = getOwnedCourse(courseId, teacherId);
		c.setCourseIsUse("N");   // 소프트 삭제
	}

	// 본인 강의만 수정/삭제 가능
	public Course getOwnedCourse(Long courseId, String teacherId) {
		Course c = getCourse(courseId);
		if (!c.getTeacher().getUserId().equals(teacherId)) {
			throw new AccessDeniedException("본인 강의만 수정/삭제할 수 있습니다.");
		}
		return c;
	}

	private void apply(Course c, CourseForm f) {
		c.setSubject(sr.findById(f.getSubjectId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다.")));
		c.setCourseTitle(f.getCourseTitle());
		c.setCourseTarget(f.getCourseTarget());
		c.setCourseStartDate(f.getCourseStartDate());
		c.setCourseEndDate(f.getCourseEndDate());
		c.setCourseCriteria(f.getCourseCriteria());
		applyTimes(c, f.getTimes());
	}

	private void applyTimes(Course c, List<CourseForm.TimeSlot> slots) {
		// 요일, 시작 교시 순으로 정렬
		List<CourseForm.TimeSlot> sorted = new ArrayList<>(slots);
		sorted.sort(Comparator.comparing(CourseForm.TimeSlot::getWeekday)
				.thenComparing(CourseForm.TimeSlot::getStartTime));

		c.getTimes().clear();            // 수정 시 기존 시간은 지우고 다시 저장 (orphanRemoval)
		for (CourseForm.TimeSlot s : sorted) {
			CourseTime t = new CourseTime();
			t.setCourse(c);
			t.setWeekday(s.getWeekday());
			t.setStartTime(s.getStartTime());
			t.setEndTime(s.getEndTime());
			c.getTimes().add(t);
		}

		// 기존 컬럼에는 첫 번째 시간을 대표값으로 유지 (시간표 등 기존 코드 호환)
		CourseForm.TimeSlot first = sorted.get(0);
		c.setCourseWeekday(first.getWeekday());
		c.setCourseStartTime(first.getStartTime());
		c.setCourseEndTime(first.getEndTime());
	}
	
	public List<Course> getTeacherCourseList(String userId) {
		return cr.findByTeacher_UserIdAndCourseIsUseOrderByCourseIdDesc(userId, "Y");
	}
}