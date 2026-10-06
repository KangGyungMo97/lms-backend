package com.hitech.lms.user;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UsersRepository usr;
	
	private final PasswordEncoder passwordEncoder;
	
	public Users create(String userId, String userName, String tempPassword, String userEmail, String userTel, String userBday, String deptId) {

			Users user = new Users();
			
			user.setUsersId(userId);                                        // PK (학번) - 직접 입력
			user.setUsersName(userName);
			user.setUsersPassword(passwordEncoder.encode(tempPassword));    // 임시 비번 암호화
			user.setUsersEmail(userEmail);
			user.setUsersTel(userTel);
			user.setUsersBday(userBday);
			user.setDeptId(deptId);
			
			user.setUsersCreated(LocalDateTime.now());                   
			user.setUsersStatus("A");                                       
			
			usr.save(user);
			return user;
		}
	
}
