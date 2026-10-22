package com.hitech.lms.domain.user;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

	private final UsersRepository ur;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

		// 학번/사번 으로 계정 찾기
		Optional<Users> _user = ur.findById(userId);

		// 예외 → 로그인 실패
		if (_user.isEmpty()) {
			throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
		}

		Users user = _user.get();

		// 권한 목록 만들기
		List<GrantedAuthority> authorities = new ArrayList<>();

		// user_role -> 권한 결정 (S 학생 / P 교수 / A 관리자)
		if ("A".equals(user.getUserRole())) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		} else if ("P".equals(user.getUserRole())) {
			authorities.add(new SimpleGrantedAuthority("ROLE_TEACHER"));
		} else {
			authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
		}

		return new User(user.getUserId(), user.getUserPassword(), authorities);
	}
}