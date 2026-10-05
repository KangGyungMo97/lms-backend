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
public class UserStudent {

	@Id
	@Column(length = 20, nullable = false)
	private String userId; // 사용자 ID
	
	@Column(length = 20, nullable = false)
	private String userName; // 사용자 이름
	
	@Column(length = 255, nullable = false)
	private String userPassword; // 사용자 비번
	
	@Column(length = 50, nullable = false)
	private String userEmail; // 이메일
	
	@Column(nullable = false)
	private LocalDateTime userCreated; // 계정생성날짜
	
	@Column(length = 30, nullable = false)
	private String userTel; // 유대폰번호
	
	@Column(length = 8, nullable = false)
	private String userBday; // 생년원일
	
	@Column(length = 1)
	private String userGrade; // 학년
	
	private LocalDateTime userLastLogin; // 최근로그인기록
	
	@Column(length = 1, nullable = false)
	private String userStatus; // 상태여부
	
	@Column(length = 20, nullable = false)
	private String deptId; // 학과 ID

}
