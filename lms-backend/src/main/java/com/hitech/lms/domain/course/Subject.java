package com.hitech.lms.domain.course;

import com.hitech.lms.domain.user.Dept;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Subject {

	@Id
	@Column(length = 20)
	private String subjectId;           // 과목 코드 (직접 입력)

	@Column(length = 50, nullable = false)
	private String subjectName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dept_id", nullable = false)
	private Dept dept;
}
