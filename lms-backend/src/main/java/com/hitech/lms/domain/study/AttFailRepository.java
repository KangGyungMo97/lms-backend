package com.hitech.lms.domain.study;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hitech.lms.domain.user.Users;

public interface AttFailRepository extends JpaRepository<AttFail, Long> {

	// 이 세션에서 이 학생이 틀린 기록
	Optional<AttFail> findBySessionAndStudent(AttSession session, Users student);

	// 이 학생의 틀린 기록 (여러 세션 한 번에 - 학생 화면)
	List<AttFail> findByStudentAndSessionIn(Users student, Collection<AttSession> sessions);
}
