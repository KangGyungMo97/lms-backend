package com.hitech.lms.domain.course;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseForm {

	@NotBlank(message = "과목명을 입력하세요.")
	@Size(max = 50)
	private String courseTitle;

	@NotBlank(message = "분류를 선택하세요.")
	private String subjectId;            // Subject의 PK 타입에 맞추세요

	@NotNull(message = "대상을 선택하세요.")
	private Integer courseTarget;

	@NotNull(message = "시작일을 입력하세요.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate courseStartDate;

	@NotNull(message = "종료일을 입력하세요.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate courseEndDate;

	@Valid
	@NotEmpty(message = "수업 시간을 한 개 이상 입력하세요.")
	private List<TimeSlot> times = new ArrayList<>();

	@Size(max = 200)
	private String courseCriteria;       // 수료 기준 (선택)

	// 수업 시간 한 줄 (요일 + 시작/종료 교시)
	@Getter
	@Setter
	public static class TimeSlot {

		@NotBlank(message = "요일을 선택하세요.")
		private String weekday;

		@NotNull(message = "시작 교시를 선택하세요.")
		private Integer startTime;

		@NotNull(message = "종료 교시를 선택하세요.")
		private Integer endTime;
	}
}