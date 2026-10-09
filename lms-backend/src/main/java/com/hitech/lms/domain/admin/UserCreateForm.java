package com.hitech.lms.domain.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {

	@Size(max = 20)
	@NotEmpty(message="학번 / 사번은 필수항목입니다.")
	private String userId;

	@NotEmpty(message="이름은 필수항목입니다.")
	private String userName;

	@NotEmpty(message="임시 비밀번호는 필수항목입니다.")
	private String password;

	@NotEmpty(message="이메일은 필수항목입니다.")
	@Email
	private String userEmail;

	@NotEmpty(message="전화번호는 필수항목입니다.")
	private String userTel;

	@NotEmpty(message="생년월일은 필수항목입니다.")
	@Pattern(regexp = "\\d{8}", message="생년월일은 8자리 숫자입니다. (예: 20070315)")
	private String userBday;

	@NotEmpty(message="학과는 필수항목입니다.")
	private String deptId;

	@NotEmpty(message="구분은 필수항목입니다.")
	private String userRole; // S(학생) / P(교수) / A(관리자)

	private String userGrade; // 학년 (학생만)

}
