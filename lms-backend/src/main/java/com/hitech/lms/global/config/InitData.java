package com.hitech.lms.global.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.admin.AdminService;
import com.hitech.lms.domain.user.Dept;
import com.hitech.lms.domain.user.DeptRepository;
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 처음 실행할 때 기본 데이터 만들기 (홍승훈)
 * - 계정 등록은 관리자만 할 수 있어서, 관리자가 한 명도 없으면 아무도 로그인할 수 없음
 * - 그래서 학과(D001)가 없으면 만들고, 관리자가 없으면 기본 관리자 1명을 자동으로 만듦
 * - 이미 관리자 계정이 있으면 아무것도 하지 않음 (기존 데이터는 건드리지 않음)
 *
 * 기본 관리자: 0000001 / 1234  ← 로그인 후 비밀번호 변경 권장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitData implements CommandLineRunner {

	private final UsersRepository ur;
	private final DeptRepository dr;
	private final UserService us;

	@Override
	@Transactional
	public void run(String... args) {
		// 학과는 스마트소프트웨어과(D001) 하나로 고정 → 없으면 만들기 (계정은 학과가 꼭 있어야 함)
		if (!dr.existsById(AdminService.DEPT_ID)) {
			Dept dept = new Dept();
			dept.setDeptId(AdminService.DEPT_ID);
			dept.setDeptName("스마트소프트웨어과");
			dept.setDeptIsgrade("Y");
			dr.save(dept);
		}

		if (ur.existsByUserRole("A")) {
			return;   // 관리자가 이미 있으면 끝
		}

		if (ur.existsById("0000001") || ur.existsByUserEmail("admin@test.com")) {
			log.warn("관리자 계정이 없지만 0000001 또는 admin@test.com 이 이미 사용 중이라 기본 관리자를 만들지 않았습니다.");
			return;
		}
		us.create("0000001", "관리자", "1234", "admin@test.com", "010-0000-0001", "19900101", AdminService.DEPT_ID, "A");
		log.info("기본 관리자 계정을 만들었습니다: 0000001 / 1234 (로그인 후 비밀번호를 바꿔 주세요)");
	}
}
