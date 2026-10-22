package com.hitech.lms.domain.study;

import java.time.LocalDateTime;

import com.hitech.lms.domain.course.ClassClock;
import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.course.Lesson;

import lombok.Getter;

// 오늘의 수업 카드 한 장 (대시보드 · 출석 화면에서 같이 씀)
//   phase : BEFORE 입장 전 (시작 10분 전보다 이름) / ON 수업 시간 (시작 10분 전 ~ 끝) / END 끝남
//   attSession : 교수가 '수업 시작'을 누르면 생김 (없으면 아직 시작 안 함)
//   lesson : 시작했으면 그 차시, 아니면 다음에 진행할 차시 (등록된 차시가 없으면 null)
@Getter
public class TodayClass {

	private final Course course;
	private final ClassClock.Slot slot;
	private final Lesson lesson;
	private final int lessonNo;          // 몇 번째 차시인지 (1부터)
	private final AttSession attSession;
	private final Att myAtt;             // 학생 - 내 출석 기록 (없으면 null)
	private final int attendCount;       // 교수 - 출석 + 지각 인원
	private final int rosterCount;       // 교수 - 수강 인원
	private final int failCount;         // 학생 - 코드 틀린 횟수 (MAX_FAIL 이면 잠김)

	public TodayClass(Course course, ClassClock.Slot slot, Lesson lesson, int lessonNo,
			AttSession attSession, Att myAtt, int attendCount, int rosterCount, int failCount) {
		this.course = course;
		this.slot = slot;
		this.lesson = lesson;
		this.lessonNo = lessonNo;
		this.attSession = attSession;
		this.myAtt = myAtt;
		this.attendCount = attendCount;
		this.rosterCount = rosterCount;
		this.failCount = failCount;
	}

	// 학생 - 코드 5회 틀려서 잠김
	public boolean isLocked() {
		return failCount >= AttService.MAX_FAIL;
	}

	// 학생 - 남은 기회
	public int getRemaining() {
		return Math.max(0, AttService.MAX_FAIL - failCount);
	}

	public String getPhase() {
		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(slot.start().minusMinutes(AttService.ENTER_BEFORE_MINUTES))) return "BEFORE";
		if (now.isBefore(slot.end())) return "ON";
		return "END";
	}

	public boolean isStarted() {
		return attSession != null;
	}

	// "10:00~12:50"
	public String getTimeLabel() {
		return slot.label();
	}

	// "10:00"
	public String getStartLabel() {
		return slot.label().substring(0, 5);
	}

	// "4차시 SQL 기초" (차시가 없으면 빈 글자)
	public String getLessonLabel() {
		if (lesson == null) return "";
		return lessonNo + "차시 " + lesson.getLessonTitle();
	}
}
