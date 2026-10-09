package com.hitech.lms.domain.study;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitForm {

	@NotBlank(message="제출 내용은 필수항목입니다.")
	private String content;

	// TODO: 첨부파일은 MultipartFile 로 받기 (FileService)

}
