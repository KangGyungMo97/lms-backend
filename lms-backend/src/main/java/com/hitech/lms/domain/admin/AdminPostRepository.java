package com.hitech.lms.domain.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminPostRepository extends JpaRepository<AdminPost, Long> {

	// 관리자 게시글(시스템 공지) 목록 - 사용 중(Y)인 글만, 제목 · 내용 검색 (홍승훈)
	@Query(value = "select p from AdminPost p join fetch p.writer "
			+ "where p.adminPostIsUse = 'Y' "
			+ "and (p.adminPostTitle like %:kw% or p.adminPostContent like %:kw%)",
	       countQuery = "select count(p) from AdminPost p "
			+ "where p.adminPostIsUse = 'Y' "
			+ "and (p.adminPostTitle like %:kw% or p.adminPostContent like %:kw%)")
	Page<AdminPost> findPostList(@Param("kw") String kw, Pageable pageable);

}
