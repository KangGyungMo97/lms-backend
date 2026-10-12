package com.hitech.lms.domain.study;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitForm {

	@NotBlank(message="제출 내용은 필수항목입니다.")
	@Size(max=2000, message="제출 내용은 2000자까지 입력할 수 있습니다.") // submit_content 길이
	private String content;

	// TODO: 첨부파일은 MultipartFile 로 받기 (FileService)

}
