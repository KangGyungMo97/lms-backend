package com.hitech.lms.domain.user;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메일 발송 (홍승훈) - 계정 등록 시 임시 비밀번호 보내기
 *
 * - 메일 서버(SMTP) 설정이 있으면 진짜 메일을 보냄
 *   (spring.mail.host / username / password 는 비밀번호가 들어가서 GitHub 에 올리지 않고 각자 환경변수로 넣음)
 * - 설정이 없으면(개발 중) 메일 대신 서버 콘솔에 [DEV MAIL] 로 출력 → 팀원 누구나 바로 테스트 가능
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

	// 메일 설정이 없으면 JavaMailSender 빈이 안 만들어짐 → ObjectProvider 로 "있으면 쓰고 없으면 콘솔"
	private final ObjectProvider<JavaMailSender> mailSender;

	/** 메일 보내기. 진짜 발송이 실패하면 IllegalStateException (계정 등록도 함께 취소됨) */
	public void send(String to, String subject, String text) {
		JavaMailSender sender = mailSender.getIfAvailable();
		if (sender == null) {
			log.info("[DEV MAIL] to={} / {} / {}", to, subject, text);
			return;
		}
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setTo(to);
			msg.setSubject(subject);
			msg.setText(text);
			sender.send(msg);
		} catch (MailException e) {
			log.error("메일 발송 실패 to={}", to, e);
			throw new IllegalStateException("메일 발송에 실패했습니다. 이메일 주소와 메일 서버 설정을 확인해 주세요.");
		}
	}
}
