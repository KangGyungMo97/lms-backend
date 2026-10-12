package com.hitech.lms.domain.board;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
	//사용중(Y)인 공지사항만 검색!
	//검색 제목 또는 내용으로 검색가능
	@Query(value = "select n from Notice n join fetch n.writer "
            + "where n.noticeIsUse = 'Y' "
            + "and (n.noticeTitle like %:kw% or n.noticeContent like %:kw%)",
           countQuery = "select count(n) from Notice n "
            + "where n.noticeIsUse = 'Y' "
            + "and (n.noticeTitle like %:kw% or n.noticeContent like %:kw%)")
	Page<Notice> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

}
