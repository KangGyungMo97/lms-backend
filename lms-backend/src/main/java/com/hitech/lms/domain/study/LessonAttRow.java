package com.hitech.lms.domain.study;

import java.time.LocalDate;

import com.hitech.lms.domain.course.Lesson;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 교수 출석부 - 차시별 출석 기록 한 줄
//   state : DONE 진행함 / ON 진행 중(출석 받는 중) / NONE 미진행
@Getter
@AllArgsConstructor
public class LessonAttRow {
	private final Lesson lesson;
	private final int no;              // 몇 번째 차시 (1부터)
	private final LocalDate date;      // 진행한 날 (미진행이면 null)
	private final String state;
	private final int present;         // 출석
	private final int late;            // 지각
	private final int excused;         // 공결
	private final int absent;          // 결석
	private final int roster;          // 수강 인원

	// 출석률 % = (출석 + 지각 + 공결) / 수강 인원 (미진행이거나 인원이 없으면 0)
	public int getRate() {
		if (roster == 0 || "NONE".equals(state)) return 0;
		return (int) Math.round((present + late + excused) * 100.0 / roster);
	}
}
