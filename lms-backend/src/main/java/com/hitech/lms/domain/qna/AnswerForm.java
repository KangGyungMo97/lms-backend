package com.hitech.lms.domain.qna;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerForm {

	@NotBlank(message="답변 내용은 필수항목입니다.")
	@Size(max=300, message="답변은 300자까지 입력할 수 있습니다.")   // answer_content 길이
	private String content;

}
