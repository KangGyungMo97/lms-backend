package com.hitech.lms.domain.study;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 달력의 날짜 칸 1개
@Getter
@AllArgsConstructor
public class AttCalDay {

	private final LocalDate date;
	private final boolean inMonth;            // 이 달의 날짜인지 (앞뒤 빈 칸을 채우는 지난달·다음달 날짜는 false)
	private final boolean today;              // 오늘인지
	private final List<AttCalLesson> lessons; // 이 날 진행한 수업들
}
