package com.hitech.lms.domain.study;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HomeworkRepository extends JpaRepository<Homework, Long> {

	// 교수 - 내가 담당하는 수업(Course.teacher)의 과제만
	// Homework → lesson → course → teacher → userId 순서로 따라가서 찾음
	Page<Homework> findByLesson_Course_Teacher_UserId(String userId, Pageable pageable);

}
