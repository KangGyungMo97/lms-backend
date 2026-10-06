package com.hitech.lms.login;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hitech.lms.user.UserStudent;
import com.hitech.lms.user.UserStudentRepository;
import com.hitech.lms.user.UserTeacher;
import com.hitech.lms.user.UserTeacherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

	private final UserStudentRepository usr;

	private final UserTeacherRepository utr; 

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

		// 학번으로 계정찾기
		Optional<UserStudent> _user = usr.findById(userName);

		// 없으면 예외 -> 로그인 실패
		if (_user.isEmpty()) {
			throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
		}
		
		UserStudent user = _user.get();

		List<GrantedAuthority> authorities = new ArrayList<>();

		// 교수 테이블에 있는지 확인
		Optional<UserTeacher> _teacher = utr.findByUserId(userName);

		if (_teacher.isEmpty()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));     // 교수 테이블에 없음 → 학생
		} else if ("Y".equals(_teacher.get().getTeacherRole())) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));       // teacher_role = Y → 관리자
		} else {
			authorities.add(new SimpleGrantedAuthority("ROLE_TEACHER"));     // teacher_role = N → 교수
		}

		// Security에 넘기기 (아이디, 암호화된 비번, 권한)
		return new User(user.getUserId(), user.getUserPassword(), authorities);
	}
}
