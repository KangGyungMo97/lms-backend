package com.hitech.lms.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Dept {
	
	@Id
	@Column(length = 20)
	private String deptId; // 학과ID
	
	@Column(length = 50)
	private String deptName; // 학과이름
	
	@Column(length =10)
	private String deptIsgrade; // 학년 여부 (학년이 없는경우 -> 하이테크)
	
	
}
