package com.hitech.lms.domain.study;

import java.time.Duration;
import java.time.LocalDateTime;

import com.hitech.lms.domain.course.Lesson;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// 출석 세션 - 교수가 '출석 시작'을 누르면 수업(Lesson)마다 1개 생김
// 6자리 코드를 들고 있고, expiresAt(시작 + 10분)이 지나면 닫힘
@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "lesson_id"))  // 한 수업은 한 번만 시작
public class AttSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sessionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lesson_id", nullable = false)
	private Lesson lesson;

	@Column(length = 6, nullable = false)
	private String code;                    // 6자리 숫자 (예: "048213")

	@Column(nullable = false)
	private LocalDateTime openedAt;         // 시작 시각 = 이 수업의 출석 날짜

	@Column(nullable = false)
	private LocalDateTime expiresAt;        // 마감 시각 (시작 + 10분)

	// 지금 입력 가능한지 (화면에서 session.open 으로 씀)
	public boolean isOpen() {
		return LocalDateTime.now().isBefore(expiresAt);
	}

	// 마감까지 남은 초 (이미 지났으면 0) - 교수 화면 카운트다운용
	public long getRemainingSeconds() {
		return Math.max(0, Duration.between(LocalDateTime.now(), expiresAt).getSeconds());
	}
}
