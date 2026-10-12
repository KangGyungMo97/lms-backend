package com.hitech.lms.domain.board;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hitech.lms.domain.file.FileAtt;
import com.hitech.lms.domain.user.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Data
public class Notice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long noticeId;

	@Column(length = 50, nullable = false)
	private String noticeTitle;

	@Column(length = 1000, nullable = false)
	private String noticeContent;

	@Column(nullable = false)
	private LocalDate noticeCreateDate;

	@Column(nullable = false)
	private LocalDate noticeUpdateDate;

	@Column(nullable = false)
	private Integer noticeViewCount = 0;        // 조회수

	@Column(length = 1, nullable = false)
	private String noticeIsUse = "Y";   // 사용 Y / 삭제 N

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users writer;               // 작성자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_att_id")
	private FileAtt fileAtt;            // 첨부파일 (없으면 NULL)
	

}

