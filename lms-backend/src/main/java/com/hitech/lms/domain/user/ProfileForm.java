package com.hitech.lms.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileForm {

	@NotEmpty(message = "전화번호는 필수항목입니다.")
	@Pattern(regexp = "01\\d-?\\d{3,4}-?\\d{4}", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
	private String userTel;

	@NotEmpty(message = "이메일은 필수항목입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Size(max = 50, message = "이메일은 50자 이하로 입력하세요.")
	private String userEmail;
}