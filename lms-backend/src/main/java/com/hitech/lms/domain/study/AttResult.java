package com.hitech.lms.domain.study;

// 수업 시작(교수) · 수업 참여(학생) 결과 (AttService.start / enter 가 돌려줌)
//   lessonId : 성공했을 때 들어갈 차시 (실패면 null)
// ※ 실패해도 예외를 던지지 않고 이 값으로 돌려줌 → 컨트롤러에서 안내 문구로 바꿈
public record AttResult(Status status, Long lessonId) {

	public enum Status {
		PRESENT,      // 출석 (시작 후 10분 안에 참여)
		LATE,         // 지각 (10분 지나서 참여)
		STARTED,      // 교수 - 수업 시작됨 (이미 시작했어도 이 값)
		ALREADY,      // 학생 - 이미 출석 · 지각 기록이 있음 → 그냥 입장
		PROCESSED,    // 학생 - 교수가 공결 · 결석으로 처리함
		NOT_STARTED,  // 학생 - 교수가 아직 수업을 시작하지 않음
		NO_CLASS,     // 오늘 이 강의 수업이 없음
		TOO_EARLY,    // 아직 입장 시간이 아님 (시작 10분 전부터)
		CLOSED,       // 수업이 끝남
		NO_LESSON,    // 교수 - 진행할 차시가 없음 (차시 등록 필요)
		NOT_MINE      // 내 강의가 아님
	}

	public static AttResult of(Status status) {
		return new AttResult(status, null);
	}

	public static AttResult of(Status status, Long lessonId) {
		return new AttResult(status, lessonId);
	}
}
