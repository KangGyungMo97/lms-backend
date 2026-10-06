package com.hitech.lms.domain.qna;

import java.time.LocalDateTime;

import com.hitech.lms.domain.course.Lesson;
import com.hitech.lms.domain.file.FileAtt;
import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class QnaBoard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long qnaId;

	@Column(length = 100, nullable = false)
	private String qnaTitle;

	@Column(length = 1000, nullable = false)
	private String qnaContent;

	@Column(nullable = false)
	private LocalDateTime qnaCreateDate;

	@Column(length = 1, nullable = false)
	private String qnaIsUse = "Y";      // 사용 Y / 삭제 N

	@Column(length = 1, nullable = false)
	private String qnaStatus;           // 답변 상태 (대기중/답변완료, 코드값 팀 협의)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users writer;               // 질문 작성자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lesson_id", nullable = false)
	private Lesson lesson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_att_id")
	private FileAtt fileAtt;            // 첨부파일 (없으면 NULL)
}
