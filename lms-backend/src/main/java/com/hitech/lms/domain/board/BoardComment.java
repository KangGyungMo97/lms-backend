package com.hitech.lms.domain.board;

import java.time.LocalDateTime;

import com.hitech.lms.domain.user.Users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 자유게시판 댓글 (홍승훈)
 * ERD 에 없던 테이블이라 새로 추가 → 테이블 이름: board_comment
 * (ddl-auto=update 라서 서버를 켜면 테이블이 자동으로 만들어짐)
 */
@Entity
@Getter
@Setter
public class BoardComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long commentId;

	@Column(length = 300, nullable = false)
	private String commentContent;

	@Column(nullable = false)
	private LocalDateTime commentCreateDate;

	private LocalDateTime commentUpdateDate;   // 수정한 적 없으면 NULL

	@Column(length = 1, nullable = false)
	private String commentIsUse = "Y";         // 사용 Y / 삭제 N

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;                       // 어느 게시글의 댓글인지

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users writer;                      // 댓글 작성자
}
