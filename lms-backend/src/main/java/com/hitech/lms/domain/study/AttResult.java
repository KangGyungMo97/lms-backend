package com.hitech.lms.domain.study;

// 수업 시작(교수) · 출석 코드 입력(학생) 결과 (AttService.start / check 가 돌려줌)
//   lessonId  : 성공했을 때 그 차시 (실패면 null)
//   remaining : 코드가 틀렸을 때 남은 기회
// ※ 실패해도 예외를 던지지 않고 이 값으로 돌려줌
//   → 예외를 던지면 @Transactional 이 롤백돼서 '틀린 횟수'가 저장되지 않기 때문
public record AttResult(Status status, Long lessonId, int remaining) {

	public enum Status {
		PRESENT,      // 출석 (시작 후 10분 안에 입력)
		LATE,         // 지각 (10분 지나서 입력)
		STARTED,      // 교수 - 수업 시작됨 (이미 시작했어도 이 값)
		WRONG,        // 코드 틀림 (remaining = 남은 기회)
		LOCKED,       // 5회 모두 틀려서 잠김
		ALREADY,      // 이미 출석 · 지각 기록이 있음
		PROCESSED,    // 교수가 공결 · 결석으로 처리함
		NOT_STARTED,  // 교수가 아직 수업을 시작하지 않음
		NO_CLASS,     // 오늘 이 강의 수업이 없음
		TOO_EARLY,    // 아직 시간이 아님 (시작 10분 전부터)
		CLOSED,       // 수업이 끝남
		NO_LESSON,    // 교수 - 진행할 차시가 없음 (차시 등록 필요)
		NOT_MINE      // 내 강의가 아님
	}

	public static AttResult of(Status status) {
		return new AttResult(status, null, 0);
	}

	public static AttResult of(Status status, Long lessonId) {
		return new AttResult(status, lessonId, 0);
	}
}
