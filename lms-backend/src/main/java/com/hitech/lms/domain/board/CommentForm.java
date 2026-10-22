package com.hitech.lms.domain.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentForm {

	@NotBlank(message="댓글 내용은 필수항목입니다.")
	@Size(max = 300, message="댓글은 300자까지 쓸 수 있습니다.")
	private String content;

}
