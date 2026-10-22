package com.hitech.lms.domain.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Lesson {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long lessonId;

	@Column(length = 50, nullable = false)
	private String lessonTitle;

	@Column(length = 200, nullable = false)
	private String lessonContent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;
}
