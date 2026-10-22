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

	
	private LocalDateTime userPwChanged;
	
	// 상태 코드 -> 한글
	public static String statusLabel(String s) {
		return switch (s) {
			case "A" -> "정상";
			case "L" -> "잠금";
			case "B" -> "차단";
			case "W" -> "탈퇴";
			default  -> s;
		};
	}

	public String getStatusLabel() {
		return statusLabel(userStatus);
	}
	
	// 연락처 마스킹 (010-1234-5678 -> 010-****-5678)
		public String getMaskedTel() {
			if (userTel == null) return "";
			return userTel.replaceAll("(\\d{2,3})-?(\\d{3,4})-?(\\d{4})", "$1-****-$3");
		}

	// 생년월일 마스킹 (20000101 -> 2000-**-**)
	public String getMaskedBday() {
		if (userBday == null || userBday.length() != 8) return "";
		return userBday.substring(0, 4) + "-**-**";
	}

	// 이메일 마스킹 (kimstudent@domain.com -> kim****@domain.com)
	public String getMaskedEmail() {
		if (userEmail == null || !userEmail.contains("@")) return "";
		int at = userEmail.indexOf('@');
		String local = userEmail.substring(0, at);
		return local.substring(0, Math.min(3, local.length())) + "****" + userEmail.substring(at);
	}
}
