package com.hitech.lms.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Users {

	@Id
	@Column(length = 20, nullable = false)
	private String usersId; // 사용자 ID
	
	@Column(length = 20, nullable = false)
	private String usersName; // 사용자 이름
	
	@Column(length = 255, nullable = false)
	private String usersPassword; // 사용자 비번
	
	@Column(length = 50, nullable = false)
	private String usersEmail; // 이메일
	
	@Column(nullable = false)
	private LocalDateTime usersCreated; // 계정생성날짜
	
	@Column(length = 30, nullable = false)
	private String usersTel; // 휴대폰번호
	
	@Column(length = 8, nullable = false)
	private String usersBday; // 생년원일
	
	@Column(length = 1)
	private String usersGrade; // 학년
	
	private LocalDateTime usersLastLogin; // 최근로그인기록
	
	@Column(length = 1, nullable = false)
	private String usersStatus; // 상태여부
	
	@Column(length = 20, nullable = false)
	private String deptId; // 학과 ID
	
	@Column(length = 1, nullable = false)
	private String users_role; // 사용자 역할(학생 s, 교수 p, 관리자 a)

}
