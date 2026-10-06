package com.hitech.lms.domain.qna;

import java.time.LocalDateTime;

import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Answer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long answerId;

	@Column(length = 300, nullable = false)
	private String answerContent;

	@Column(nullable = false)
	private LocalDateTime answerCreateDate;

	@Column(length = 1, nullable = false)
	private String answerIsUse = "Y";   // 사용 Y / 삭제 N

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "qna_id", nullable = false)
	private QnaBoard qnaBoard;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users writer;               // 답변 작성자 (교수)
}
