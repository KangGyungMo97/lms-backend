package com.hitech.lms.domain.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Timetable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long timetableId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;
}
