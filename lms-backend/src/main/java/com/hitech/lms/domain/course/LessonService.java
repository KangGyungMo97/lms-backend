package com.hitech.lms.domain.course;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

	private final LessonRepository lr;
	private final CourseRepository cr;

	// 목록 - 페이징(10개)
	public Page<Lesson> getList(int page) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("lessonId")));
		return lr.findAll(pageable);
	}

	// 상세 - findById, 없으면 DataNotFoundException
	public Lesson getLesson(Long id) {
		return lr.findById(id)
				.orElseThrow(() -> new DataNotFoundException("해당 정보가 없습니다!"));
	}

	// 등록 폼 드롭다운 - 로그인한 교수 본인의 사용중 강좌
	public List<Course> getMyCourses(String userId) {
		return cr.findByTeacher_UserIdAndCourseIsUseOrderByCourseIdDesc(userId, "Y");
	}

	// [추가] 본인 수업 조회 (수정 화면용) - 본인 수업이 아니면 403
	public Lesson getMyLesson(Long id, String userId) {
		Lesson lesson = getLesson(id);
		checkOwner(lesson, userId);
		return lesson;
	}

	// 등록 - 검증 실패 시 IllegalArgumentException(메시지를 화면에 표시)
	@Transactional
	public void create(Long courseId, String lessonTitle, String lessonContent, String userId) {
		if (courseId == null) {
			throw new IllegalArgumentException("강좌를 선택하세요.");
		}
		validate(lessonTitle, lessonContent);

		Course course = cr.findById(courseId)
				.orElseThrow(() -> new DataNotFoundException("강좌를 찾을 수 없습니다!"));
		// 폼 값 변조 방지: 본인 담당 + 사용중 강좌만 허용
		if (!course.getTeacher().getUserId().equals(userId) || !"Y".equals(course.getCourseIsUse())) {
			throw new IllegalArgumentException("본인이 담당하는 강좌에만 수업을 등록할 수 있습니다.");
		}

		Lesson lesson = new Lesson();
		lesson.setCourse(course);
		lesson.setLessonTitle(lessonTitle);
		lesson.setLessonContent(lessonContent);
		lr.save(lesson);
	}

	// [추가] 수정 - 제목/내용만 변경 (강좌는 변경 불가)
	@Transactional
	public void modify(Long id, String lessonTitle, String lessonContent, String userId) {
		validate(lessonTitle, lessonContent);
		Lesson lesson = getMyLesson(id, userId);
		lesson.setLessonTitle(lessonTitle);
		lesson.setLessonContent(lessonContent);
		lr.save(lesson);
	}

	// [추가] 삭제 - 본인 수업만
	@Transactional
	public void delete(Long id, String userId) {
		Lesson lesson = getMyLesson(id, userId);
		lr.delete(lesson);
	}

	// [추가] 제목/내용 검증 (등록·수정 공용)
	private void validate(String lessonTitle, String lessonContent) {
		if (lessonTitle == null || lessonTitle.isBlank()) {
			throw new IllegalArgumentException("수업 제목은 필수항목입니다.");
		}
		if (lessonTitle.length() > 50) {
			throw new IllegalArgumentException("수업 제목은 50자 이내로 입력하세요.");
		}
		if (lessonContent == null || lessonContent.isBlank()) {
			throw new IllegalArgumentException("수업 내용은 필수항목입니다.");
		}
		if (lessonContent.length() > 200) {
			throw new IllegalArgumentException("수업 내용은 200자 이내로 입력하세요.");
		}
	}

	// [추가] 담당 교수 본인인지 확인 - 아니면 403
	private void checkOwner(Lesson lesson, String userId) {
		if (!lesson.getCourse().getTeacher().getUserId().equals(userId)) {
			throw new AccessDeniedException("본인이 담당하는 수업만 수정/삭제할 수 있습니다.");
		}
	}
}