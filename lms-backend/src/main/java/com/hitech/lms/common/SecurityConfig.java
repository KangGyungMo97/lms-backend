package com.hitech.lms.common;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.config.Customizer;

@Configuration // 파일이 스프링의 환경 설정 파일임을 의미
@EnableWebSecurity // 모든 요청 URL이 스프링 시큐리티의 제어를 받도록
public class SecurityConfig {
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		
		http
			.authorizeHttpRequests(
					(authorizeHttpRequests) -> authorizeHttpRequests
					.requestMatchers("/**").permitAll())
					
					// CSRF 처리 시 H2 콘솔은 예외로 처리
					.csrf((csrf)->csrf.ignoringRequestMatchers("/h2-console/**"))
					
					// X-Frame-Options 헤더를 DENY 대신 SAMEORIGIN으로 설정
			        .headers((headers) -> headers
			                .addHeaderWriter(new XFrameOptionsHeaderWriter(
			                    XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))

			        // 로그인: Security 기본 로그인 화면 사용
			        .formLogin(Customizer.withDefaults());
		
		return http.build();
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
