package com.hitech.lms.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration      
@EnableWebSecurity  
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		
			// 지금은 전체 허용 (권한별 제한은 나중에)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/**").permitAll())

			// h2-console은 CSRF 검사 제외
			.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))

			// h2-console 화면이 iframe으로 뜰 수 있게
			.headers(headers -> headers
				.addHeaderWriter(new XFrameOptionsHeaderWriter(
					XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))

			// Security 기본 로그인 화면 사용
			.formLogin(Customizer.withDefaults());

		return http.build();
	}

	// 비밀번호 암호화 (회원 등록 + 로그인 비교에 같이 사용)
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}