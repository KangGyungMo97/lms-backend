package com.hitech.lms.domain.board;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	private final BoardRepository br;

	// 목록 - 최신순 + 페이징(10개) + 검색 
	public Page<Board> getList(int page, String kw) {
		return null;
	}

	// 상세 - findById, 없으면 DataNotFoundException
	public Board getBoard(Long id) {
		return null;
	}

	// 조회수 +1
	public void increaseViewCount(Board board) {

	}

	// 작성 - 작성일 now, 작성자 넣고 저장
	public void create(String title, String content, Users writer) {

	}

	// 수정 - 수정일 now
	public void modify(Board board, String title, String content) {

	}

	// 삭제 - board_is_use 를 "N" 으로 바꿀지, 진짜 delete 할지 팀 협의
	public void delete(Board board) {

	}

	// ===== 댓글 =====
	// ERD 에 댓글 테이블이 없음 -> 엔티티 추가 후 파라미터 / 반환 타입 맞추기

	public void createComment(Board board, String content, Users writer) {

	}

	public void modifyComment(Long commentId, String content) {

	}

	public void deleteComment(Long commentId) {

	}

}
