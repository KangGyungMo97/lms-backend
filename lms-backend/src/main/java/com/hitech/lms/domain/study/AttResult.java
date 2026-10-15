package com.hitech.lms.domain.study;

// 출석 코드 입력 결과 (AttService.check 가 돌려줌)
// ※ 코드가 틀려도 예외를 던지지 않고 이 값으로 돌려줌
//   → 예외를 던지면 @Transactional 이 롤백돼서 '틀린 횟수'가 저장되지 않기 때문
public record AttResult(Status status, int remaining) {

	public enum Status {
		SUCCESS,      // 출석 완료
		WRONG,        // 코드 틀림 (remaining = 남은 기회)
		LOCKED,       // 5회 모두 틀려서 잠김
		CLOSED,       // 10분이 지나 닫힘
		ALREADY,      // 이미 출석함 (출석 · 지각)
		PROCESSED,    // 교수가 이미 상태를 처리함 (공결 · 결석)
		NOT_STARTED   // 교수가 아직 시작 안 함
	}

	public static AttResult of(Status status) {
		return new AttResult(status, 0);
	}
}
