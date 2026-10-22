package com.hitech.lms.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Dept {

	@Id
	@Column(length = 20)
	private String deptId;          // 학과 코드 (직접 입력)

	@Column(length = 50, nullable = false)
	private String deptName;        // 학과명

	@Column(length = 1, nullable = false)
	private String deptIsgrade;     // 학년이 없는 과정이나 교수의 경우
}
