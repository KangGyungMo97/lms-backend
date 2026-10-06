package com.hitech.lms.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTeacherRepository extends JpaRepository<UserTeacher ,String> {
	
	Optional<UserTeacher> findByUserId(String userId); 
	
}
