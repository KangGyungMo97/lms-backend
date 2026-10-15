package com.hitech.lms.domain.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CourseTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long courseTimeId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(length = 1, nullable = false)
	private String weekday;          // 월 1 ~ 일 7

	@Column(nullable = false)
	private Integer startTime;       // 시작 교시

	@Column(nullable = false)
	private Integer endTime;         // 종료 교시
}