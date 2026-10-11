package com.hitech.lms.domain.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardForm {

	// DB 컬럼 길이에 맞춤 (board_title 100, board_content 1000)
	@NotBlank(message="제목은 필수항목입니다.")
	@Size(max=100, message="제목은 100자까지 입력할 수 있습니다.")
	private String title;

	@NotBlank(message="내용은 필수항목입니다.")
	@Size(max=1000, message="내용은 1000자까지 입력할 수 있습니다.")
	private String content;

}
