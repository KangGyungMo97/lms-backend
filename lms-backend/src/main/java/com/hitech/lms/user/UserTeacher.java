package com.hitech.lms.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserTeacher {
	
	@Id
	@Column(length = 20, nullable = false)
	private String userTeacherId; // 교수 ID
	
	@Column(length = 1, nullable = false)
	private String teacherRole; // 권한여부
	
	@Column(length = 20, nullable = false, unique = true)
	private String userId; // 사용자 ID

}
