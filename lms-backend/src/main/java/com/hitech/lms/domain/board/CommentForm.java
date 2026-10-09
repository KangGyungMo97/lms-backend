package com.hitech.lms.domain.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentForm {

	@NotBlank(message="댓글 내용은 필수항목입니다.")
	private String content;

}
