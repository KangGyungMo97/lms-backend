package com.hitech.lms.domain.board;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

	// 관리자 대시보드 - 최근 게시글 5개 (작성자 함께 조회) (홍승훈)
	@EntityGraph(attributePaths = "writer")
	List<Board> findTop5ByBoardIsUseOrderByBoardIdDesc(String boardIsUse);

	// 사용 중(Y)인 게시글만, 제목 · 내용 · 작성자 이름으로 검색
	// kw 가 "" 이면 like '%%' 가 되어 전체 목록이 나옴
	@Query(value = "select b from Board b join fetch b.writer u "
			+ "where b.boardIsUse = 'Y' "
			+ "and (b.boardTitle like %:kw% or b.boardContent like %:kw% or u.userName like %:kw%)",
	       countQuery = "select count(b) from Board b join b.writer u "
			+ "where b.boardIsUse = 'Y' "
			+ "and (b.boardTitle like %:kw% or b.boardContent like %:kw% or u.userName like %:kw%)")
	Page<Board> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

}
