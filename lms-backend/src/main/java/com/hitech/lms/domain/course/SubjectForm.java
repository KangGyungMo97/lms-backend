package com.hitech.lms.domain.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectForm {

	@NotBlank(message="과목 코드는 필수항목입니다.")
	@Size(max=20)
	private String subjectId;

	@NotBlank(message="과목명은 필수항목입니다.")
	private String subjectName;

	@NotBlank(message="학과는 필수항목입니다.")
	private String deptId;

}
