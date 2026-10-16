package com.hitech.lms.domain.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordForm {

	@NotEmpty(message = "현재 비밀번호를 입력하세요.")
	private String currentPassword;

	@NotEmpty(message = "새 비밀번호를 입력하세요.")
	@Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d).{8,50}", message = "영문과 숫자를 포함해 8자 이상 입력하세요.")
	private String newPassword;

	@NotEmpty(message = "새 비밀번호 확인을 입력하세요.")
	private String newPasswordConfirm;
}