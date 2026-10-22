package com.hitech.lms.domain.study;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hitech.lms.domain.file.FileAtt;
import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Submit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long submitId;

	@Column(length = 2000)
	private String submitContent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users student;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "homework_id", nullable = false)
	private Homework homework;

	@Column(nullable = false)
	private LocalDateTime submitDate;

	private Integer submitScore;

	private LocalDate submitScoreDate;

	@Column(length = 1, nullable = false)
	private String submitIsDone = "N";  // 미제출 N / 제출완료 Y / 제출마감 F / 채점완료 R

	@Column(length = 200)
	private String submitFeedback;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_att_id")
	private FileAtt fileAtt;            // 첨부파일 (없으면 NULL)
}
