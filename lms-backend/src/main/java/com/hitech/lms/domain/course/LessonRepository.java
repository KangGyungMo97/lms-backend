package com.hitech.lms.domain.course;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

	List<Lesson> findByCourse_Teacher_UserIdOrderByLessonIdDesc(String userId);

}
