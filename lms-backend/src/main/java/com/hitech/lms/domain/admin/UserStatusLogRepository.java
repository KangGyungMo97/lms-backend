package com.hitech.lms.domain.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hitech.lms.domain.user.Users;

public interface UserStatusLogRepository extends JpaRepository<UserStatusLog, Long> {
	
	List<UserStatusLog> findByUserOrderByChangedAtDesc(Users user);

}
