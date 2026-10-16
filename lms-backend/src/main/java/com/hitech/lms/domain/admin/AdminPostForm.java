package com.hitech.lms.domain.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 관리자 게시글(시스템 공지) 등록 · 수정 폼 (홍승훈) */
@Getter
@Setter
public class AdminPostForm {

	@NotBlank(message="제목은 필수항목입니다.")
	@Size(max = 100, message="제목은 100자까지 입력할 수 있습니다.")
	private String title;

	@NotBlank(message="내용은 필수항목입니다.")
	@Size(max = 1000, message="내용은 1000자까지 입력할 수 있습니다.")
	private String content;

}
