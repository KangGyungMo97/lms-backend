package com.hitech.lms.domain.study;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeForm {

	@NotNull(message="점수는 필수항목입니다.")
	@Min(0)
	@Max(100)
	private Integer score;

	private String feedback; // 피드백 (선택)

}
