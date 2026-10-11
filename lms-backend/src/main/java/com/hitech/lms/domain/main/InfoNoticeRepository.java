package com.hitech.lms.domain.main;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.hitech.lms.domain.admin.AdminPost;

/**
 * 정보마당 시스템 공지 "조회 전용" Repository (홍승훈)
 * - 관리자 게시글(admin_post) 테이블을 읽기만 함 (등록 · 수정 · 삭제는 관리자 기능)
 * - 김경모님 AdminPostRepository / AdminService 를 건드리지 않으려고 따로 만듦
 *   (나중에 AdminService.getPostList / getPost 가 완성되면 그쪽으로 합쳐도 됨)
 */
public interface InfoNoticeRepository extends Repository<AdminPost, Long> {

	// 사용 중(Y)인 공지만, 제목으로 검색 + 페이징
	@Query(value = "select p from AdminPost p join fetch p.writer "
			+ "where p.adminPostIsUse = 'Y' and p.adminPostTitle like %:kw%",
	       countQuery = "select count(p) from AdminPost p "
			+ "where p.adminPostIsUse = 'Y' and p.adminPostTitle like %:kw%")
	Page<AdminPost> findNoticeList(@Param("kw") String kw, Pageable pageable);

	// 상세 (사용 중인 공지만)
	@Query("select p from AdminPost p join fetch p.writer where p.adminPostId = :id and p.adminPostIsUse = 'Y'")
	Optional<AdminPost> findNotice(@Param("id") Long id);

	// 이전 글 = 지금 글보다 번호가 작은 것 중 가장 큰 글
	Optional<AdminPost> findFirstByAdminPostIdLessThanAndAdminPostIsUseOrderByAdminPostIdDesc(Long id, String isUse);

	// 다음 글 = 지금 글보다 번호가 큰 것 중 가장 작은 글
	Optional<AdminPost> findFirstByAdminPostIdGreaterThanAndAdminPostIsUseOrderByAdminPostIdAsc(Long id, String isUse);

	// 조회수 +1 (DB 에서 바로 더해서 동시에 여러 명이 봐도 숫자가 안 빠짐)
	@Modifying
	@Query("update AdminPost p set p.adminPostView = p.adminPostView + 1 where p.adminPostId = :id")
	int increaseView(@Param("id") Long id);

}
