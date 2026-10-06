package com.hitech.lms.domain.board;

import java.time.LocalDate;

import com.hitech.lms.domain.file.FileAtt;
import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Board {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long boardId;

	@Column(length = 100, nullable = false)
	private String boardTitle;

	@Column(length = 1000, nullable = false)
	private String boardContent;

	@Column(nullable = false)
	private LocalDate boardCreateDate;

	@Column(nullable = false)
	private LocalDate boardUpdateDate;

	@Column(nullable = false)
	private Integer boardViewCount = 0;        // 조회수

	@Column(length = 1, nullable = false)
	private String boardIsUse = "Y";   // 사용 Y / 삭제 N

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users writer;               // 작성자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_att_id")
	private FileAtt fileAtt;            // 첨부파일 (없으면 NULL)
}
