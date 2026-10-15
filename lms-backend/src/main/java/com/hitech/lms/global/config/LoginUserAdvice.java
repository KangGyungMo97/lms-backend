package com.hitech.lms.global.config;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.hitech.lms.domain.user.Users;
import com.hitech.lms.domain.user.UsersRepository;

import lombok.RequiredArgsConstructor;

// 모든 화면(layout.html 헤더)에서 쓸 '로그인한 사람 이름'
//   로그인 정보(principal)에는 학번/사번만 있어서, 이름은 DB 에서 찾아 model 에 넣어 줌
//   화면에서는 ${loginName} 으로 사용 (로그인 안 했으면 null)
@ControllerAdvice
@RequiredArgsConstructor
public class LoginUserAdvice {

	private final UsersRepository ur;

	@ModelAttribute("loginName")
	public String loginName(Principal principal) {
		if (principal == null) return null;
		return ur.findById(principal.getName())
				.map(Users::getUserName)
				.orElse(principal.getName());     // 혹시 못 찾으면 학번이라도 표시
	}
}
