package com.hitech.lms.domain.board;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

	// 이 게시글의 댓글 목록 - 사용 중(Y)인 것만, 먼저 단 순서
	List<BoardComment> findByBoardAndCommentIsUseOrderByCommentIdAsc(Board board, String commentIsUse);

	// 목록 화면용 - 게시글 여러 개의 댓글 수를 한 번에 세기 (결과: [게시글ID, 댓글 수])
	@Query("select c.board.boardId, count(c) from BoardComment c "
			+ "where c.board.boardId in :ids and c.commentIsUse = 'Y' "
			+ "group by c.board.boardId")
	List<Object[]> countByBoardIds(@Param("ids") List<Long> ids);

}
