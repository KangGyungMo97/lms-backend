package com.hitech.lms.domain.file;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FileAtt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long fileAttId;

	@Column(nullable = false)
	private LocalDateTime fileAttCreateDate;

	@Column(length = 1, nullable = false)
	private String fileAttIsUse = "Y";   // 사용 Y / 삭제 N
}
