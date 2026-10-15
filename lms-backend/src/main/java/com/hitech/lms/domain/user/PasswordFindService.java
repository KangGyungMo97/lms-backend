package com.hitech.lms.domain.user;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordFindService {

	private static final int CODE_VALID_SECONDS = 180;    // 인증번호 유효시간 3분
	private static final int RESEND_WAIT_SECONDS = 60;    // 재발송 대기 60초
	private static final int MAX_FAIL = 5;                // 인증번호 최대 실패 횟수

	// 영문 + 숫자 + 특수문자 포함 8~16자
	private static final Pattern PW_PATTERN =
			Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,16}$");

	private final UsersRepository ur;
	private final SmsService smsService;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom random = new SecureRandom();

	/** 1단계: 학번/사번 확인 */
	public PwFindState start(String userId) {
		Users user = ur.findById(userId.trim())
				.orElseThrow(() -> new IllegalArgumentException("일치하는 계정이 없습니다."));
		checkStatus(user);

		PwFindState state = new PwFindState();
		state.setUserId(user.getUserId());
		state.setStep(2);
		return state;
	}

	/** 2단계: 인증번호 발송 (입력한 번호가 등록된 번호와 같아야 함) */
	public void sendCode(PwFindState state, String phone) {
		Users user = getUser(state);

		if (digits(phone).isEmpty() || !digits(phone).equals(digits(user.getUserTel()))) {
			throw new IllegalArgumentException("계정에 등록된 휴대폰 번호와 일치하지 않습니다.");
		}

		long now = System.currentTimeMillis();
		long waitMs = RESEND_WAIT_SECONDS * 1000L - (now - state.getLastSentAt());
		if (waitMs > 0) {
			throw new IllegalArgumentException("인증번호는 " + (waitMs / 1000 + 1) + "초 후에 다시 요청할 수 있습니다.");
		}

		String code = String.format("%06d", random.nextInt(1_000_000));
		smsService.send(user.getUserTel(), "[LMS] 인증번호는 [" + code + "] 입니다.");

		state.setCode(code);
		state.setExpireAt(now + CODE_VALID_SECONDS * 1000L);
		state.setLastSentAt(now);
		state.setFailCount(0);
		state.setVerified(false);
	}

	/** 2단계: 인증번호 확인 */
	public void verifyCode(PwFindState state, String input) {
		if (!state.isCodeSent()) {
			throw new IllegalArgumentException("먼저 인증번호를 받아 주세요.");
		}
		if (System.currentTimeMillis() > state.getExpireAt()) {
			throw new IllegalArgumentException("인증 시간이 만료되었습니다. 인증번호를 다시 요청해 주세요.");
		}
		if (state.getFailCount() >= MAX_FAIL) {
			throw new IllegalArgumentException("인증 시도 횟수를 초과했습니다. 인증번호를 다시 요청해 주세요.");
		}
		if (!state.getCode().equals(input.trim())) {
			state.setFailCount(state.getFailCount() + 1);
			throw new IllegalArgumentException("인증번호가 일치하지 않습니다. (" + state.getFailCount() + "/" + MAX_FAIL + ")");
		}
		state.setVerified(true);
	}

	/** 3단계: 새 비밀번호 설정 */
	@Transactional
	public void resetPassword(PwFindState state, String newPassword, String confirm) {
		if (!state.isVerified()) {
			throw new IllegalArgumentException("휴대폰 인증을 먼저 완료해 주세요.");
		}
		if (!newPassword.equals(confirm)) {
			throw new IllegalArgumentException("새 비밀번호와 확인이 일치하지 않습니다.");
		}
		if (!PW_PATTERN.matcher(newPassword).matches()) {
			throw new IllegalArgumentException("영문, 숫자, 특수문자를 포함한 8~16자로 입력해 주세요.");
		}

		Users user = getUser(state);
		checkStatus(user);

		if (passwordEncoder.matches(newPassword, user.getUserPassword())) {
			throw new IllegalArgumentException("이전에 사용한 비밀번호는 사용할 수 없습니다.");
		}

		user.setUserPassword(passwordEncoder.encode(newPassword));
		user.setUserPwChanged(LocalDateTime.now());
		ur.save(user);
	}

	private Users getUser(PwFindState state) {
		return ur.findById(state.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
	}

	// 차단 / 탈퇴 계정은 재설정 불가
	private void checkStatus(Users user) {
		if ("B".equals(user.getUserStatus()) || "W".equals(user.getUserStatus())) {
			throw new IllegalArgumentException("비밀번호를 재설정할 수 없는 계정입니다. 관리자에게 문의해 주세요.");
		}
	}

	// 숫자만 남기기 (010-1234-5678 -> 01012345678)
	private String digits(String s) {
		return s == null ? "" : s.replaceAll("\\D", "");
	}
}