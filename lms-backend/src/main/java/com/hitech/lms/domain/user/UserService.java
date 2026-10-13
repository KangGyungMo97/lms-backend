package com.hitech.lms.domain.user;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UsersRepository ur;
	private final DeptRepository dr;
	private final PasswordEncoder passwordEncoder;

	/** 관리자 계정 등록: role = S(학생) / P(교수) / A(관리자) */
	/*계정상태 : status =A(정상)/잠금(L)/차단(B)/탈퇴W */
	public Users create(String userId, String userName, String tempPassword, String userEmail, String userTel, String userBday, String deptId, String userRole) {

		Dept dept = dr.findById(deptId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학과입니다: " + deptId));

		Users user = new Users();
		user.setUserId(userId);
		user.setUserName(userName);
		user.setUserPassword(passwordEncoder.encode(tempPassword));   // 임시 비번 암호화
		user.setUserEmail(userEmail);
		user.setUserTel(userTel);
		user.setUserBday(userBday);
		user.setDept(dept);
		user.setUserRole(userRole);
		user.setUserCreated(LocalDateTime.now());
		user.setUserStatus("A");                                  

		return ur.save(user);
	}

	/** 로그인한 사용자 찾기 (principal.getName() = 학번/사번) */
	public Users getUser(String userId) {
		return ur.findById(userId)
				.orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));
	}
	
	/** 아이디(학번/사번) 찾기: 이름 + 생년월일 + 구분(S/P) */
	public List<String> findUserIds(String userName, String userBday, String userRole) {

	    if (!"S".equals(userRole) && !"P".equals(userRole)) {
	        throw new IllegalArgumentException("구분 값이 올바르지 않습니다.");
	    }
	    if (!userBday.matches("\\d{8}")) {
	        throw new IllegalArgumentException("생년월일은 8자리 숫자로 입력해 주세요.");
	    }

	    List<Users> users = ur.findByUserNameAndUserBdayAndUserRole(userName.trim(), userBday, userRole);
	    if (users.isEmpty()) {
	        throw new DataNotFoundException("일치하는 계정이 없습니다.");
	    }

	    return users.stream()
	    		.map(Users::getUserId)
	            .toList();
	}
	/** 임시 비밀번호 발급: 암호화해서 저장하고, 평문은 한 번만 반환 */
	public String resetPassword(String userId) {
		Users user = getUser(userId);

		String temp = generateTempPassword(10);
		user.setUserPassword(passwordEncoder.encode(temp));
		user.setUserPwChanged(LocalDateTime.now());
		ur.save(user);

		return temp;
	}

	private String generateTempPassword(int length) {
		// 헷갈리는 문자(0, O, 1, l, I)는 제외
		String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}
}
