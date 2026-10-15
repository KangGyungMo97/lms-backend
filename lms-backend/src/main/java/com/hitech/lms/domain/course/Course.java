package com.hitech.lms.domain.course;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.hitech.lms.domain.user.Users;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
	private String courseContent = "";   // 폼에서는 안 쓰지만 기존 컬럼이 NOT NULL이라 기본값 유지

	@Column(length = 1, nullable = false)
	private String courseIsUse = "Y";    // 사용 Y / 삭제 N

	@Column(length = 1, nullable = false)
	private String courseWeekday;        // 월 1 ~ 일 7

	@Column(nullable = false)
	private Integer courseStartTime;     // 시작 교시

	@Column(nullable = false)
	private Integer courseEndTime;       // 종료 교시

	private Integer courseTarget;        // 대상 학년 (1~4)

	private LocalDate courseStartDate;   // 수업 시작일

	private LocalDate courseEndDate;     // 수업 종료일

	@Column(length = 200)
	private String courseCriteria;       // 수료 기준

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users teacher;     // 담당 교수
	
	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("weekday ASC, startTime ASC")
	@BatchSize(size = 50)
	private List<CourseTime> times = new ArrayList<>();
}