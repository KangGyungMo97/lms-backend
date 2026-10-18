package com.hitech.lms.domain.study;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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



	// 첨부파일 (여러 개 선택 가능, 안 골라도 됨) - <input type="file" name="files" multiple>
	private List<MultipartFile> files = new ArrayList<>();
}
