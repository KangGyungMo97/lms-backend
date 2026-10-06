package com.hitech.lms.domain.course;

import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long courseId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject;

	@Column(length = 50, nullable = false)
	private String courseTitle;

	@Column(length = 500, nullable = false)
	private String courseContent;

	@Column(length = 1, nullable = false)
	private String courseIsUse = "Y";   // 사용 Y / 삭제 N

	@Column(length = 1, nullable = false)
	private String courseWeekday;       // 월 1 ~ 일 7

	@Column(nullable = false)
	private Integer courseStartTime;    // 시작 교시

	@Column(nullable = false)
	private Integer courseEndTime;      // 종료 교시

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users teacher;              // 담당 교수
}
