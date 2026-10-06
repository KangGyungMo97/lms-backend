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

import com.hitech.lms.user.Users;
import com.hitech.lms.user.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

	private final UsersRepository usr;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

		// 학번으로 계정 
		Optional<Users> _user = usr.findById(userName);

		// 없으면 예외
		if (_user.isEmpty()) {
			throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
		}

		Users user = _user.get();
 
		// 권한 목록 만들기
		List<GrantedAuthority> authorities = new ArrayList<>();

		// Security에 넘기기 (아이디, 암호화된 비번, 권한)
		return new User(user.getUsersId(), user.getUsersPassword(), authorities);
	}
}
