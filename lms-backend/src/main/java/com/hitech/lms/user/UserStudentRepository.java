package com.hitech.lms.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStudentRepository extends JpaRepository<UserStudent ,String> {
	
	
}
