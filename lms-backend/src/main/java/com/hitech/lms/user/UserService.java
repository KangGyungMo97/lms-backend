package com.hitech.lms.user;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserStudentRepository usr;
	
	private final UserTeacherRepository utr;
	
	private final PasswordEncoder passwordEncoder;
	
	public UserStudent create(String userId, String userName, String tempPassword, String userEmail, String userTel, String userBday, String deptId) {

			UserStudent user = new UserStudent();
			
			user.setUserId(userId);                                        // PK (학번) - 직접 입력
			user.setUserName(userName);
			user.setUserPassword(passwordEncoder.encode(tempPassword));    // 임시 비번 암호화
			user.setUserEmail(userEmail);
			user.setUserTel(userTel);
			user.setUserBday(userBday);
			user.setDeptId(deptId);
			
			user.setUserCreated(LocalDateTime.now());                   
			user.setUserStatus("A");                                       
			
			usr.save(user);
			return user;
		}
	
}
