package com.hitech.lms.domain.course;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
	
	List<Lesson> findByCourse_Teacher_UserIdOrderByLessonIdDesc(String userId);
	 
	// [수정] 목록에 과목명을 표시하므로 "course.subject" 추가
	@EntityGraph(attributePaths = {"course", "course.teacher", "course.subject"})
	Page<Lesson> findAll(Pageable pageable);
}
