package com.hitech.lms.domain.course;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

	@EntityGraph(attributePaths = {"subject"})
	List<Course> findByTeacher_UserIdAndCourseIsUseOrderByCourseIdDesc(String userId, String courseIsUse);

	@EntityGraph(attributePaths = {"subject", "teacher"})
	Page<Course> findByTeacher_UserIdAndCourseIsUse(String userId, String courseIsUse, Pageable pageable);

	@EntityGraph(attributePaths = {"subject", "teacher"})
	Optional<Course> findByCourseId(Long courseId);
	
	@EntityGraph(attributePaths = {"subject", "teacher"})
	List<Course> findByCourseIsUse(String courseIsUse);
}