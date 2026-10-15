package com.hitech.lms.domain.main;

import com.hitech.lms.domain.study.Homework;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 대시보드 - 과제 한 줄
//   학생 state : 미제출 / 제출완료 / 채점완료 / 마감
//   교수 state : 채점 대기 / 진행 중 / 채점 완료
@Getter
@AllArgsConstructor
public class DashHomeworkRow {
	private final Homework homework;
	private final String state;
	private final int submitted;        // 교수 - 제출 인원
	private final int rosterCount;      // 교수 - 수강 인원
	private final int ungraded;         // 교수 - 채점 안 한 제출물 수
}
