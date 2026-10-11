package com.hitech.lms.domain.study;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hitech.lms.domain.user.Users;

public interface SubmitRepository extends JpaRepository<Submit, Long> {

	// 이 과제에 대한 내 제출물 (과제 상세 - 학생)
	Optional<Submit> findByHomeworkAndStudent(Homework homework, Users student);

	// 내가 제출한 전체 목록 (과제 목록의 '제출 여부' 칸 - 학생)
	List<Submit> findByStudent(Users student);

}
