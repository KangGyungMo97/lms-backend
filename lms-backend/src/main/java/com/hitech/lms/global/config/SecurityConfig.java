package com.hitech.lms.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration      // 스프링 설정 파일
@EnableWebSecurity  // 모든 요청을 Spring Security가 관리
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 사용
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// 접근 권한
			.authorizeHttpRequests(auth -> auth
				// 각 권한만 접근
				.requestMatchers("/dashboard/student/**").hasRole("STUDENT")
				.requestMatchers("/dashboard/teacher/**").hasRole("TEACHER")
				.requestMatchers("/dashboard/admin/**").hasRole("ADMIN")
				.requestMatchers("/dashboard").authenticated()
				// 관리자만
				.requestMatchers("/admin/**").hasRole("ADMIN")
				// 나머지는 전체 허용 (메서드마다 @PreAuthorize 로 체크)
				.anyRequest().permitAll())

			// h2-console은 CSRF 검사 제외
			.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))

			// h2-console 화면이 iframe으로 뜰 수 있게
			.headers(headers -> headers
				.addHeaderWriter(new XFrameOptionsHeaderWriter(
					XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))

			// 로그인 (SBB 방식)
			.formLogin(form -> form
				.loginPage("/user/login")                    // GET  /user/login → login.html
				.defaultSuccessUrl("/dashboard", true)       // 성공하면 /dashboard → 권한별로 이동
				.failureUrl("/user/login?error")             // 실패하면 에러 메시지
				.permitAll())

			// 로그아웃 (SBB 방식 - GET /user/logout)
			.logout(logout -> logout
				.logoutRequestMatcher(
					PathPatternRequestMatcher
					.withDefaults()
					.matcher(HttpMethod.GET, "/user/logout"))
				.logoutSuccessUrl("/user/login?logout")
				.invalidateHttpSession(true));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
