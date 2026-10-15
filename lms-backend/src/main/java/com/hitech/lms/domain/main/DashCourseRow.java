package com.hitech.lms.domain.main;

import com.hitech.lms.domain.course.Course;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 대시보드 - 수강 중 과목(학생) / 담당 과목(교수) 한 줄
@Getter
@AllArgsConstructor
public class DashCourseRow {
	private final Course course;
	private final String timeLabel;     // "월·수 10:00~12:50"
	private final int progress;         // 진도율 % (강의 기간 중 지난 날짜 비율)
	private final String state;         // 예정 / 진행 중 / 종료
	private final int rosterCount;      // 교수 - 수강 인원
}
