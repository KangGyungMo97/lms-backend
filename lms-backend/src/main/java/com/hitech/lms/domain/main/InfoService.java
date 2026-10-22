package com.hitech.lms.domain.main;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hitech.lms.domain.admin.AdminPost;

import lombok.RequiredArgsConstructor;

/**
 * 정보마당 - 시스템 공지 조회 (INF-01, INF-02) (홍승훈)
 */
@Service
@RequiredArgsConstructor
public class InfoService {

	private final InfoNoticeRepository nr;

	// 목록 - 최신순 + 페이징(10개) + 제목 검색
	public Page<AdminPost> getNoticeList(int page, String kw) {
		page = Math.max(page, 0);                       // ?page=-1 같은 잘못된 값 방지
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("adminPostId"));      // 최신 글이 위로
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		return nr.findNoticeList(kw, pageable);
	}

	// 상세 - 없거나 삭제(N)된 공지면 404
	// TODO: global/error 에 DataNotFoundException 이 생기면 그걸로 바꾸기
	public AdminPost getNotice(Long id) {
		return nr.findNotice(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다."));
	}

	// 조회수 +1
	@Transactional
	public void increaseViewCount(AdminPost notice) {
		nr.increaseView(notice.getAdminPostId());
		notice.setAdminPostView(notice.getAdminPostView() + 1);   // 화면에도 바로 올라간 숫자로 보이게
	}

	// 이전 글 (없으면 null)
	public AdminPost getPrev(Long id) {
		return nr.findFirstByAdminPostIdLessThanAndAdminPostIsUseOrderByAdminPostIdDesc(id, "Y").orElse(null);
	}

	// 다음 글 (없으면 null)
	public AdminPost getNext(Long id) {
		return nr.findFirstByAdminPostIdGreaterThanAndAdminPostIsUseOrderByAdminPostIdAsc(id, "Y").orElse(null);
	}

}
