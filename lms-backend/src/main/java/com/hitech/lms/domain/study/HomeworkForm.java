package com.hitech.lms.domain.study;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;


import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeworkForm {

	@NotNull(message="수업을 선택해주세요.")
	private Long lessonId;

	@NotBlank(message="제목은 필수항목입니다.")
	@Size(max=200)
	private String title;

	@NotBlank(message="내용은 필수항목입니다.")
	private String content;

	@NotNull(message="시작일은 필수항목입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd") // <input type="date"> 값 받기
	private LocalDate startDate;

	@NotNull(message="마감일은 필수항목입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;


	// 첨부파일 (여러 개 선택 가능, 안 골라도 됨) - <input type="file" name="files" multiple>
	private List<MultipartFile> files = new ArrayList<>();
}
