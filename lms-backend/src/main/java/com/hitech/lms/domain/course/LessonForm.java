package com.hitech.lms.domain.course;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonForm {

    @NotNull(message = "강좌를 선택하세요.")
    private Long courseId;

    @NotEmpty(message = "수업 제목은 필수 항목입니다.")
    @Size(max = 200, message = "수업 제목은 200자 이하로 입력해주세요.")
    private String title;

    @NotEmpty(message = "수업 내용은 필수 항목입니다.")
    @Size(max = 1000, message = "수업 내용은 1000자 이하로 입력해주세요.")
    private String content;
}
