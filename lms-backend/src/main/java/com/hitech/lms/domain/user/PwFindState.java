package com.hitech.lms.domain.user;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PwFindState implements Serializable {

	private String userId;
	private int step = 1;            // 1 학번/사번 입력 / 2 휴대폰 인증 / 3 새 비밀번호 설정
	private String code;             // 발송한 인증번호
	private long expireAt;           // 인증번호 만료 시각 (epoch millis)
	private long lastSentAt;         // 마지막 발송 시각 (재발송 대기용)
	private int failCount;           // 인증번호 틀린 횟수
	private boolean verified;        // 인증 완료 여부

	public boolean isCodeSent() {
		return code != null;
	}

	// 화면 타이머용: 인증번호 만료까지 남은 초
	public long getRemainSeconds() {
		if (code == null) return 0;
		return Math.max(0, (expireAt - System.currentTimeMillis()) / 1000);
	}
}