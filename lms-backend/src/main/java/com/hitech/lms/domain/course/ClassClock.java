package com.hitech.lms.domain.course;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// 강의의 '교시'를 실제 시각으로 바꿔 주는 도우미 (출석 · 대시보드에서 같이 씀)
//   교시 시각표는 CourseController.START / END 를 그대로 사용 (1교시 09:00~09:50 ...)
public final class ClassClock {

	private ClassClock() { }

	// 오늘 수업 한 칸 - 시작 · 끝 시각 + 교시
	public record Slot(LocalDateTime start, LocalDateTime end, int startPeriod, int endPeriod) {

		// "10:00~12:50"
		public String label() {
			return CourseController.START.get(startPeriod) + "~" + CourseController.END.get(endPeriod);
		}
	}

	// 이 날짜에 이 강의 수업이 있으면 그 시간 (없으면 비어 있음)
	//   - 강의 기간(시작일~종료일) 밖이면 수업 없음
	//   - 같은 요일에 시간이 두 번 있으면 가장 이른 시작 ~ 가장 늦은 끝으로 합침
	public static Optional<Slot> on(Course c, LocalDate date) {
		if (c.getCourseStartDate() != null && date.isBefore(c.getCourseStartDate())) return Optional.empty();
		if (c.getCourseEndDate() != null && date.isAfter(c.getCourseEndDate())) return Optional.empty();

		String weekday = String.valueOf(date.getDayOfWeek().getValue());   // 월 1 ~ 일 7
		int start = Integer.MAX_VALUE, end = 0;

		if (c.getTimes().isEmpty()) {             // 예전 방식으로 등록된 강의 (요일 · 교시가 Course 에 있음)
			if (weekday.equals(c.getCourseWeekday())) {
				start = c.getCourseStartTime();
				end = c.getCourseEndTime();
			}
		} else {
			for (CourseTime t : c.getTimes()) {
				if (!weekday.equals(t.getWeekday())) continue;
				start = Math.min(start, t.getStartTime());
				end = Math.max(end, t.getEndTime());
			}
		}
		if (end == 0) return Optional.empty();

		return Optional.of(new Slot(
				LocalDateTime.of(date, LocalTime.parse(CourseController.START.get(start))),
				LocalDateTime.of(date, LocalTime.parse(CourseController.END.get(end))),
				start, end));
	}

	// 오늘 수업
	public static Optional<Slot> today(Course c) {
		return on(c, LocalDate.now());
	}

	// 목록용 수업 시간 글자 - "월·수 10:00~12:50" (요일마다 시간이 다르면 "월 10:00~11:50, 수 13:00~14:50")
	public static String label(Course c) {
		Map<String, String> byDay = new LinkedHashMap<>();
		if (c.getTimes().isEmpty()) {
			if (c.getCourseWeekday() != null) {
				byDay.put(c.getCourseWeekday(),
						CourseController.START.get(c.getCourseStartTime()) + "~" + CourseController.END.get(c.getCourseEndTime()));
			}
		} else {
			for (CourseTime t : c.getTimes()) {
				byDay.merge(t.getWeekday(),
						CourseController.START.get(t.getStartTime()) + "~" + CourseController.END.get(t.getEndTime()),
						(a, b) -> a + ", " + b);
			}
		}
		if (byDay.isEmpty()) return "-";

		// 모든 요일의 시간이 같으면 요일만 묶어서 표시
		if (byDay.values().stream().distinct().count() == 1) {
			List<String> days = new ArrayList<>();
			byDay.keySet().forEach(d -> days.add(CourseController.WEEKDAY.get(d)));
			return String.join("·", days) + " " + byDay.values().iterator().next();
		}
		List<String> parts = new ArrayList<>();
		byDay.forEach((d, time) -> parts.add(CourseController.WEEKDAY.get(d) + " " + time));
		return String.join(", ", parts);
	}
}
