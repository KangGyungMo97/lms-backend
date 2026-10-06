package com.hitech.lms.domain.user;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Users {

	@Id
	@Column(length = 20)
	private String userId;              // 학번 / 사번 (직접 입력)

	@Column(length = 20, nullable = false)
	private String userName;

	@Column(length = 255, nullable = false)
	private String userPassword;        // BCrypt 암호화

	@Column(length = 50, nullable = false, unique = true)
	private String userEmail;

	@Column(nullable = false)
	private LocalDateTime userCreated;

	@Column(length = 30, nullable = false)
	private String userTel;

	@Column(length = 8, nullable = false)
	private String userBday;            // yyyyMMdd

	@Column(length = 1)
	private String userGrade;           // 학년 (교수·관리자는 NULL)

	private LocalDateTime userLastLogin;

	@Column(length = 1, nullable = false)
	private String userStatus;          // 정상/잠금/차단/탈퇴 (코드값 팀 협의)

	@Column(length = 1, nullable = false)
	private String userRole = "S";      // 학생 S / 교수 P / 관리자 A

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dept_id", nullable = false)
	private Dept dept;
}
