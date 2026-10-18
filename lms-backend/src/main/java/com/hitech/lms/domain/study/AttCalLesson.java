package com.hitech.lms.domain.study;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 달력 날짜 칸 안의 수업 1개 - 제목 + 그 수업의 출석 현황 (인원수)
@Getter
@AllArgsConstructor
public class AttCalLesson {

	private final Long lessonId;
	private final String title;

	private final int present;   // 출석
	private final int late;      // 지각
	private final int excused;   // 공결
	private final int absent;    // 결석
}
