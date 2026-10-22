package com.hitech.lms.domain.board;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeForm {
	
    private String subjectId; // 대소문자 명시 확인 (subjectId)

    @NotEmpty(message = "제목은 필수 항목입니다.")
    @Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
    private String title;

    @NotEmpty(message = "내용은 필수 항목입니다.")
    @Size(max = 1000, message = "내용은 1000자 이하로 입력해주세요.")
    private String content;

	// 첨부파일은 MultipartFile 로 받기 (FileService)

}
