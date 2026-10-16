package com.hitech.lms.domain.user;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {

	private final UserService us;
	private final UsersRepository ur;
	private final PasswordEncoder passwordEncoder;

	public Users getUser(String userId) {
		return us.getUser(userId);
	}

	// 연락처 수정 (휴대폰, 이메일)
	@Transactional
	public void updateProfile(String userId, ProfileForm f) {
		Users u = us.getUser(userId);

		String email = f.getUserEmail().trim();
		if (!email.equals(u.getUserEmail()) && ur.existsByUserEmail(email)) {   // 본인 이메일은 제외
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		u.setUserTel(normalizeTel(f.getUserTel().trim()));
		u.setUserEmail(email);
		ur.save(u);
	}

	// 비밀번호 변경을 하는쪽
	@Transactional
	public void changePassword(String userId, PasswordForm f) {
		Users u = us.getUser(userId);

		if (!passwordEncoder.matches(f.getCurrentPassword(), u.getUserPassword())) {
			throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
		}
		if (f.getNewPassword().equals(f.getCurrentPassword())) {
			throw new IllegalArgumentException("현재 비밀번호와 다른 비밀번호를 입력하세요.");
		}
		if (f.getNewPassword().equals(u.getUserBday())) {   // 생년월일은 비밀번호로 사용 불가
			throw new IllegalArgumentException("생년월일은 비밀번호로 사용할 수 없습니다.");
		}

		u.setUserPassword(passwordEncoder.encode(f.getNewPassword()));
		u.setUserPwChanged(LocalDateTime.now());
		ur.save(u);
	}

	// 전화번호를 010-1234-5678 형태로 통일
	private String normalizeTel(String tel) {
		String d = tel.replaceAll("\\D", "");
		if (d.length() == 11) return d.substring(0, 3) + "-" + d.substring(3, 7) + "-" + d.substring(7);
		if (d.length() == 10) return d.substring(0, 3) + "-" + d.substring(3, 6) + "-" + d.substring(6);
		return tel;
	}
}