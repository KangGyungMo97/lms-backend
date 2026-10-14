package com.hitech.lms.domain.user;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LogSmsService implements SmsService {

	// 개발용: 실제 발송 대신 콘솔에 출력 (실제 문자 서비스 연동 시 이 클래스를 교체)
	@Override
	public void send(String phone, String message) {
		log.info("[DEV SMS] to={} / {}", phone, message);
	}
}