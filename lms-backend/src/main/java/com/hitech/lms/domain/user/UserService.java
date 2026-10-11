package com.hitech.lms.domain.user;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UsersRepository ur;
	private final DeptRepository dr;
	private final PasswordEncoder passwordEncoder;

	/** 관리자 계정 등록: role = S(학생) / P(교수) / A(관리자) */
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
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
	}
}
