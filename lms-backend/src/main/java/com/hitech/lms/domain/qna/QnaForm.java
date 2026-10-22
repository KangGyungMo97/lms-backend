package com.hitech.lms.domain.qna;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaForm {

	@NotNull(message="수업을 선택해주세요.")
	private Long lessonId;

	@NotBlank(message="제목은 필수항목입니다.")
	@Size(max=100, message="제목은 100자까지 입력할 수 있습니다.")   // qna_title 길이
	private String title;

	@NotBlank(message="내용은 필수항목입니다.")
	@Size(max=1000, message="내용은 1000자까지 입력할 수 있습니다.") // qna_content 길이
	private String content;

	// TODO: 첨부파일은 MultipartFile 로 받기 (FileService)

}
