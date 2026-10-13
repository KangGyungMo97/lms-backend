package com.hitech.lms.domain.board;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

	// 이 게시글의 댓글 목록 - 사용 중(Y)인 것만, 먼저 단 순서
	List<BoardComment> findByBoardAndCommentIsUseOrderByCommentIdAsc(Board board, String commentIsUse);

}
