package com.hitech.lms.domain.board;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository nr;

	// 목록 - 최신순 + 페이징(10개) + 검색  (QuestionService.getList 참고)
	public Page<Notice> getList(int page, String kw) {
		return null;
	}

	// 상세 - findById, 없으면 DataNotFoundException
	public Notice getNotice(Long id) {
		return null;
	}

	// 조회수 +1
	public void increaseViewCount(Notice notice) {

	}

	// 작성 - 작성일 now, 작성자 넣고 저장
	public void create(String title, String content, Users writer) {

	}

	// 수정 - 수정일 now
	public void modify(Notice notice, String title, String content) {

	}

	// 삭제 - notice_is_use 를 "N" 으로 바꿀지, 진짜 delete 할지 팀 협의
	public void delete(Notice notice) {

	}

}
