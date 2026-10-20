package com.hitech.lms.domain.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeForm {
	

	@NotNull(message = "과목을 선택하세요.")
	private Long courseId;

	@NotBlank(message="제목은 필수항목입니다.")
	@Size(max=200)
	private String title;

	@NotBlank(message="내용은 필수항목입니다.")
	@Size(max=1000)
	private String content;

	// 첨부파일은 MultipartFile 로 받기 (FileService)

}
