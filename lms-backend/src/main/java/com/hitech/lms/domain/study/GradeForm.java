package com.hitech.lms.domain.study;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeForm {

	@NotNull(message="점수는 필수항목입니다.")
	@Min(0)
	@Max(100)
	private Integer score;

	@Size(max=200, message="피드백은 200자까지 입력할 수 있습니다.") // submit_feedback 길이
	private String feedback; // 피드백 (선택)

}
