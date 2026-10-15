package com.hitech.lms.domain.course;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

	private final CourseRepository cr;

	private record Entry(String weekday, int start, int end, TimetableGrid.Block block) {}

	// ---------- 학년도/학기 ----------
	// 1학기: 3/1 ~ 8/31, 2학기: 9/1 ~ 다음해 2월 말 (1~2월은 전년도 2학기)
	public int[] currentSemester() {
		LocalDate t = LocalDate.now();
		int m = t.getMonthValue();
		int year = m >= 3 ? t.getYear() : t.getYear() - 1;
		int sem = (m >= 3 && m <= 8) ? 1 : 2;
		return new int[] { year, sem };
	}

	public Map<String, String> getSemesterOptions() {
		int[] cur = currentSemester();
		Map<String, String> m = new LinkedHashMap<>();
		for (int y = cur[0] + 1; y >= cur[0] - 1; y--) {
			for (int s = 2; s >= 1; s--) {
				m.put(y + "-" + s, y + "학년도 " + s + "학기");
			}
		}
		return m;
	}

	public String normalizeSemester(String semester) {
		if (semester != null && getSemesterOptions().containsKey(semester)) return semester;
		int[] cur = currentSemester();
		return cur[0] + "-" + cur[1];
	}

	// ---------- 시간표 만들기 ----------
	public TimetableGrid build(String userId, boolean teacher, boolean all, String semester) {
		String[] sp = semester.split("-");
		int year = Integer.parseInt(sp[0]);
		int sem = Integer.parseInt(sp[1]);
		LocalDate from = sem == 1 ? LocalDate.of(year, 3, 1) : LocalDate.of(year, 9, 1);
		LocalDate to = sem == 1 ? LocalDate.of(year, 8, 31) : LocalDate.of(year + 1, 3, 1).minusDays(1);

		List<Course> courses;
		if (!teacher) {
			// [임시 수정] 학생 계정이어도 우선 DB의 전체 강의를 다 가져와서 학년 필터가 먹히는지 테스트합니다.
			courses = cr.findByCourseIsUse("Y"); 
		} else if (all) {
			courses = cr.findByCourseIsUse("Y");
		} else {
			courses = cr.findByTeacher_UserIdAndCourseIsUseOrderByCourseIdDesc(userId, "Y");
		}


		// 1) 강의별 수업 시간을 요일별로 모으기
		Map<String, List<Entry>> byDay = new HashMap<>();
		for (Course c : courses) {
			if (!overlaps(c, from, to)) continue;

			TimetableGrid.Block block = new TimetableGrid.Block(
					c.getCourseId(), c.getCourseTitle(),
					c.getTeacher().getUserName(), c.getSubject().getSubjectName(),
					(int) (c.getCourseId() % 4));

			if (c.getTimes().isEmpty()) {   // 이전 방식으로 등록된 강의
				add(byDay, c.getCourseWeekday(), c.getCourseStartTime(), c.getCourseEndTime(), block);
			} else {
				for (CourseTime t : c.getTimes()) {
					add(byDay, t.getWeekday(), t.getStartTime(), t.getEndTime(), block);
				}
			}
		}

		// 2) 표의 열(월~금, 토/일은 수업이 있을 때만)과 행(기본 9교시) 정하기
		Set<String> days = new TreeSet<>(List.of("1", "2", "3", "4", "5"));
		days.addAll(byDay.keySet());
		int maxPeriod = 9;
		for (List<Entry> list : byDay.values()) {
			for (Entry e : list) maxPeriod = Math.max(maxPeriod, e.end());
		}

		// 3) 요일별로 칸 만들기 (겹치는 시간은 한 칸에 쌓기)
		TimetableGrid grid = new TimetableGrid(new ArrayList<>(days), maxPeriod);
		for (String d : days) {
			List<Entry> list = new ArrayList<>(byDay.getOrDefault(d, List.of()));
			list.sort(Comparator.comparingInt(Entry::start).thenComparingInt(Entry::end));

			TimetableGrid.Cell cur = null;
			for (Entry e : list) {
				if (cur != null && e.start() <= cur.getEndPeriod()) {
					cur.add(e.block(), e.end());
				} else {
					cur = new TimetableGrid.Cell(e.start(), e.end());
					cur.add(e.block(), e.end());
					grid.putCell(d, cur);
				}
			}
		}
		grid.markSkipped();
		return grid;
	}

	private void add(Map<String, List<Entry>> byDay, String weekday, Integer s, Integer e,
	                 TimetableGrid.Block block) {
		if (weekday == null || s == null || e == null || e < s) return;
		byDay.computeIfAbsent(weekday, k -> new ArrayList<>()).add(new Entry(weekday, s, e, block));
	}

	// 수업 기간이 학기와 겹치는지 (기간이 없는 예전 강의는 항상 표시)
	private boolean overlaps(Course c, LocalDate from, LocalDate to) {
		if (c.getCourseStartDate() == null || c.getCourseEndDate() == null) return true;
		return !c.getCourseStartDate().isAfter(to) && !c.getCourseEndDate().isBefore(from);
	}
	
	public TimetableGrid build(String userId, boolean teacher, boolean all, String semester, String grade) {
		String[] sp = semester.split("-");
		int year = Integer.parseInt(sp[0]);
		int sem = Integer.parseInt(sp[1]);
        
		// 1. 학기 날짜 계산 로직 정확하게 교정 (오타 및 매핑 수정)
		LocalDate from = (sem == 1) ? LocalDate.of(year, 3, 1) : LocalDate.of(year, 9, 1);
		LocalDate to = (sem == 1) ? LocalDate.of(year, 8, 31) : LocalDate.of(year + 1, 3, 1).minusDays(1);

		List<Course> courses;
		if (!teacher) {
			courses = cr.findByCourseIsUse("Y");   
		} else if (all) {
			courses = cr.findByCourseIsUse("Y");
		} else {
			courses = cr.findByTeacher_UserIdAndCourseIsUseOrderByCourseIdDesc(userId, "Y");
		}

		// 2. 학년 필터링 구조 유지
		if (grade != null && !grade.trim().isEmpty()) {
			String searchGrade = grade.trim(); // 사용자가 선택한 값 (예: "1")
			
			courses = courses.stream()
					.filter(c -> {
						if (c.getCourseTarget() == null) return false;
						
						// 어떠한 타입이든 안전하게 문자열로 변환합니다. (예: 1 -> "1", "1학년" -> "1학년")
						String target = String.valueOf(c.getCourseTarget()).trim();
						
						// 서로 완전히 일치하거나, 어느 한쪽이 다른 쪽을 포함하고 있으면 통과시킵니다.
						return target.equals(searchGrade) || target.contains(searchGrade) || searchGrade.contains(target);
					})
					.toList();
		}


		// 1) 강의별 수업 시간을 요일별로 모으기
		Map<String, List<Entry>> byDay = new HashMap<>();
		for (Course c : courses) {
			if (!overlaps(c, from, to)) continue;

			TimetableGrid.Block block = new TimetableGrid.Block(
					c.getCourseId(), c.getCourseTitle(),
					c.getTeacher().getUserName(), c.getSubject().getSubjectName(),
					(int) (c.getCourseId() % 4));

			if (c.getTimes().isEmpty()) {   // 이전 방식으로 등록된 강의
				add(byDay, c.getCourseWeekday(), c.getCourseStartTime(), c.getCourseEndTime(), block);
			} else {
				for (CourseTime t : c.getTimes()) {
					add(byDay, t.getWeekday(), t.getStartTime(), t.getEndTime(), block);
				}
			}
		}

		// 2) 표의 열(월~금, 토/일은 수업이 있을 때만)과 행(기본 9교시) 정하기
		Set<String> days = new TreeSet<>(List.of("1", "2", "3", "4", "5"));
		days.addAll(byDay.keySet());
		int maxPeriod = 9;
		for (List<Entry> list : byDay.values()) {
			for (Entry e : list) maxPeriod = Math.max(maxPeriod, e.end());
		}

		// 3) 요일별로 칸 만들기 (겹치는 시간은 한 칸에 쌓기)
		TimetableGrid grid = new TimetableGrid(new ArrayList<>(days), maxPeriod);
		for (String d : days) {
			List<Entry> list = new ArrayList<>(byDay.getOrDefault(d, List.of()));
			list.sort(Comparator.comparingInt(Entry::start).thenComparingInt(Entry::end));

			TimetableGrid.Cell cur = null;
			for (Entry e : list) {
				if (cur != null && e.start() <= cur.getEndPeriod()) {
					cur.add(e.block(), e.end());
				} else {
					cur = new TimetableGrid.Cell(e.start(), e.end());
					cur.add(e.block(), e.end());
					grid.putCell(d, cur);
				}
			}
		}
		grid.markSkipped();
		return grid;
	}
}