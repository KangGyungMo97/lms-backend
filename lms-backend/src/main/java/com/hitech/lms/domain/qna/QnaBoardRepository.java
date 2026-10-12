package com.hitech.lms.domain.qna;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QnaBoardRepository extends JpaRepository<QnaBoard, Long> {

	// 사용 중(Y)인 질문만, 제목 · 내용 · 작성자 이름 · 수업 제목으로 검색
	// kw 가 "" 이면 like '%%' 가 되어 전체 목록이 나옴 (자유게시판과 같은 방식)
	@Query(value = "select q from QnaBoard q join fetch q.writer u join fetch q.lesson l "
			+ "where q.qnaIsUse = 'Y' "
			+ "and (q.qnaTitle like %:kw% or q.qnaContent like %:kw% or u.userName like %:kw% or l.lessonTitle like %:kw%)",
	       countQuery = "select count(q) from QnaBoard q join q.writer u join q.lesson l "
			+ "where q.qnaIsUse = 'Y' "
			+ "and (q.qnaTitle like %:kw% or q.qnaContent like %:kw% or u.userName like %:kw% or l.lessonTitle like %:kw%)")
	Page<QnaBoard> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

}
