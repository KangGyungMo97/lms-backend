package com.hitech.lms.domain.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hitech.lms.domain.user.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter	
public class UserStatusLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long logId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users user;

	@Column(length = 1, nullable = false)
	private String beforeStatus;

	@Column(length = 1, nullable = false)
	private String afterStatus;

	@Column(length = 500)
	private String reason;                 // 사유

	private LocalDate blockStart;          // 차단 기간 (차단일 때만)
	private LocalDate blockEnd;

	@Column(nullable = false)
	private LocalDateTime changedAt;

	@Column(length = 20, nullable = false)
	private String changedBy;              // 처리자 학번/사번 (시스템이면 SYSTEM)

	public String getBeforeLabel() { return Users.statusLabel(beforeStatus); }
	public String getAfterLabel()  { return Users.statusLabel(afterStatus); }
}
