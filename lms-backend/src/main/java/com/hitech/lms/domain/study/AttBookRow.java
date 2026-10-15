package com.hitech.lms.domain.study;

import java.util.Map;

import com.hitech.lms.domain.user.Users;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 출석부 한 줄 = 학생 1명
// cells : { 수업번호 : 상태 }  Y 출석 / L 지각 / E 공결 / N 결석 / - 아직 진행 안 한 수업
@Getter
@AllArgsConstructor
public class AttBookRow {

	private final Users student;
	private final Map<Long, String> cells;

	private final int present;   // 출석 횟수
	private final int late;      // 지각 횟수
	private final int excused;   // 공결 횟수
	private final int absent;    // 결석 횟수
}
