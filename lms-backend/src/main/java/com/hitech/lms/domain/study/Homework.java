package com.hitech.lms.domain.study;

import java.time.LocalDate;

import com.hitech.lms.domain.course.Lesson;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Homework {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long homeworkId;

	@Column(length = 100, nullable = false)
	private String homeworkTitle;

	@Column(length = 1000, nullable = false)
	private String homeworkContent;

	@Column(nullable = false)
	private LocalDate homeworkStartDate;

	@Column(nullable = false)
	private LocalDate homeworkEndDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lesson_id", nullable = false)
	private Lesson lesson;
}
