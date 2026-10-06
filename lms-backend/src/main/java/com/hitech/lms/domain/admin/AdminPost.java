package com.hitech.lms.domain.admin;

import java.time.LocalDate;

import com.hitech.lms.domain.file.FileAtt;
import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** 관리자 게시글 (admin_post) */
@Entity
@Getter
@Setter
public class AdminPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long adminPostId;

	@Column(length = 100, nullable = false)
	private String adminPostTitle;

	@Column(length = 1000, nullable = false)
	private String adminPostContent;

	@Column(nullable = false)
	private LocalDate adminPostCreateDate;

	@Column(nullable = false)
	private LocalDate adminPostUpdateDate;

	@Column(nullable = false)
	private Integer adminPostView = 0;        // 조회수

	@Column(length = 1, nullable = false)
	private String adminPostIsUse = "Y";   // 사용 Y / 삭제 N

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users writer;               // 작성자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_att_id")
	private FileAtt fileAtt;            // 첨부파일 (없으면 NULL)
}
