package com.hitech.lms.domain.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {

	@Size(max = 20, message="학번 / 사번은 20자까지 입력할 수 있습니다.")
	@NotBlank(message="학번 / 사번은 필수항목입니다.")
	private String userId;

	@NotBlank(message="이름은 필수항목입니다.")
	@Size(max = 20, message="이름은 20자까지 입력할 수 있습니다.")
	private String userName;

	// 비밀번호는 관리자가 입력하지 않음 → [임시 비밀번호 발급] 버튼을 눌러야 true
	// 등록할 때 서버가 임의의 임시 비밀번호를 만들어 사용자 이메일로만 보냄 (관리자 화면에는 안 보임)
	private boolean tempPwIssued;

	@NotEmpty(message="이메일은 필수항목입니다.")
	@Email(message="이메일 형식이 올바르지 않습니다.")
	@Size(max = 50, message="이메일은 50자까지 입력할 수 있습니다.")
	private String userEmail;

	@NotEmpty(message="전화번호는 필수항목입니다.")
	@Pattern(regexp = "01\\d-?\\d{3,4}-?\\d{4}", message="전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
	private String userTel;

	@NotEmpty(message="생년월일은 필수항목입니다.")
	@Pattern(regexp = "\\d{8}", message="생년월일은 8자리 숫자입니다. (예: 20070315)")
	private String userBday;

	// 학과는 스마트소프트웨어과(D001) 하나로 고정 → 화면에서 고르지 않음 (AdminService.DEPT_ID)
	private String deptId;

	@NotEmpty(message="구분은 필수항목입니다.")
	private String userRole; // S(학생) / P(교수) / A(관리자)

	private String userGrade; // 학년 1 / 2 (학생만 필수, 교수 · 관리자는 NULL)

}
