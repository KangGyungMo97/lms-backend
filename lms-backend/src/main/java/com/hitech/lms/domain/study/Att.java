package com.hitech.lms.domain.study;

import java.time.LocalDateTime;

import com.hitech.lms.domain.course.Lesson;
import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "lesson_id" }))
public class Att {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long attId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users student;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lesson_id", nullable = false)
	private Lesson lesson;

	@Column(length = 1, nullable = false)
	private String attStatus;           // 출석 Y / 미출석 N / 지각 L / 공결 E

	@Column(nullable = false)
	private LocalDateTime attDate;      // 출석일 + 출석시간
}
