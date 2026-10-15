package com.hitech.lms.domain.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class TimetableGrid {

	private final List<String> weekdays;   // 표의 열 ("1"=월 ...)
	private final int maxPeriod;           // 표의 행 수 (교시)
	private final Map<String, Cell> cells = new HashMap<>();
	private final Set<String> skipped = new HashSet<>();   // 위 칸에 합쳐져서 그리지 않는 칸

	public TimetableGrid(List<String> weekdays, int maxPeriod) {
		this.weekdays = weekdays;
		this.maxPeriod = maxPeriod;
	}

	public Cell cell(String weekday, int period) {
		return cells.get(weekday + "-" + period);
	}

	public boolean isSkipped(String weekday, int period) {
		return skipped.contains(weekday + "-" + period);
	}

	void putCell(String weekday, Cell cell) {
		cells.put(weekday + "-" + cell.getStartPeriod(), cell);
	}

	void markSkipped() {
		cells.forEach((key, cell) -> {
			String d = key.substring(0, key.indexOf('-'));
			for (int p = cell.getStartPeriod() + 1; p <= cell.getEndPeriod(); p++) {
				skipped.add(d + "-" + p);
			}
		});
	}

	// 한 칸: 여러 교시에 걸칠 수 있고, 시간이 겹치는 강의는 한 칸에 쌓는다
	@Getter
	public static class Cell {
		private final int startPeriod;
		private int endPeriod;
		private final List<Block> blocks = new ArrayList<>();

		Cell(int startPeriod, int endPeriod) {
			this.startPeriod = startPeriod;
			this.endPeriod = endPeriod;
		}

		void add(Block b, int end) {
			boolean exists = blocks.stream().anyMatch(x -> x.getCourseId().equals(b.getCourseId()));
			if (!exists) blocks.add(b);
			if (end > endPeriod) endPeriod = end;
		}

		public int getRowspan() {
			return endPeriod - startPeriod + 1;
		}
	}

	// 칸 안에 표시할 강의 한 개
	@Getter
	@AllArgsConstructor
	public static class Block {
		private final Long courseId;
		private final String title;
		private final String teacherName;
		private final String subjectName;
		private final int color;           // 0~3, 강의별 색상 번호
	}
}