package com.hitech.lms.domain.study;

import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// 코드 틀린 횟수 - (출석 세션, 학생)마다 1행
// 로그인 세션이 아니라 DB에 저장해야 로그아웃 후 다시 들어와도 횟수가 초기화되지 않음
@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "session_id", "user_id" }))
public class AttFail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long failId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private AttSession session;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users student;

	@Column(nullable = false)
	private Integer failCount = 0;          // 틀린 횟수 (5가 되면 이번 수업은 잠김)
}
