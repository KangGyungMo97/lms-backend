package com.hitech.lms.domain.board;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.hitech.lms.domain.user.Users;
import com.hitech.lms.domain.user.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	private final BoardRepository br;

	private final UsersRepository ur;

	private final BoardCommentRepository cr;   // 댓글

	// 목록 - 최신순 + 페이징(10개) + 검색
	public Page<Board> getList(int page, String kw) {
		page = Math.max(page, 0);                // ?page=-1 같은 잘못된 값 방지
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("boardId"));   // 작성일이 날짜(LocalDate)라 같은 날 글 순서를 위해 id 로 정렬
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		return br.findAllByKeyword(kw, pageable);
	}

	// 상세 - findById, 없으면 DataNotFoundException
	// TODO: global/error 에 DataNotFoundException 이 생기면 그걸로 바꾸기 (지금은 404 로 대신 처리)
	public Board getBoard(Long id) {
		Board board = br.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
		if (!"Y".equals(board.getBoardIsUse())) {   // 삭제(N)된 글은 없는 글로 취급
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
		}
		return board;
	}

	// 조회수 +1
	@Transactional
	public void increaseViewCount(Board board) {
		board.setBoardViewCount(board.getBoardViewCount() + 1);
		br.save(board);
	}

	// 작성 - 작성일 now, 작성자 넣고 저장
	@Transactional
	public void create(String title, String content, Users writer) {
		Board board = new Board();
		board.setBoardTitle(title);
		board.setBoardContent(content);
		board.setBoardCreateDate(LocalDate.now());
		board.setBoardUpdateDate(LocalDate.now());
		board.setWriter(writer);
		br.save(board);
	}

	// 수정 - 수정일 now
	@Transactional
	public void modify(Board board, String title, String content) {
		board.setBoardTitle(title);
		board.setBoardContent(content);
		board.setBoardUpdateDate(LocalDate.now());
		br.save(board);
	}

	// 삭제 - board_is_use 를 "N" 으로 바꿀지, 진짜 delete 할지 팀 협의
	// 지금은 board_is_use = "N" (목록 · 상세에서 안 보이게) 으로 처리. 진짜 삭제로 정해지면 br.delete(board) 로 바꾸면 됨
	@Transactional
	public void delete(Board board) {
		board.setBoardIsUse("N");
		br.save(board);
	}

	// 로그인한 사용자 찾기 (principal.getName() = 학번/사번)
	public Users getUser(String userId) {
		return ur.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
	}

	// ===== 댓글 =====

	// 이 글의 댓글 목록 (삭제 안 된 것만, 오래된 순)
	public List<BoardComment> getCommentList(Board board) {
		return cr.findByBoardAndCommentIsUseOrderByCommentIdAsc(board, "Y");
	}

	// 댓글 하나 찾기 - 없거나 삭제(N)된 댓글이면 404
	public BoardComment getComment(Long commentId) {
		BoardComment comment = cr.findById(commentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
		// 삭제된 댓글이거나, 댓글이 달린 게시글이 삭제된 경우도 없는 댓글로 취급
		if (!"Y".equals(comment.getCommentIsUse()) || !"Y".equals(comment.getBoard().getBoardIsUse())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");
		}
		return comment;
	}

	// 댓글 작성 - 작성일 now, 게시글 · 작성자 넣고 저장
	@Transactional
	public BoardComment createComment(Board board, String content, Users writer) {
		BoardComment comment = new BoardComment();
		comment.setCommentContent(content);
		comment.setCommentCreateDate(LocalDateTime.now());
		comment.setBoard(board);
		comment.setWriter(writer);
		return cr.save(comment);
	}

	// 댓글 수정 - 수정일 now
	@Transactional
	public void modifyComment(BoardComment comment, String content) {
		comment.setCommentContent(content);
		comment.setCommentUpdateDate(LocalDateTime.now());
		cr.save(comment);
	}

	// 댓글 삭제 - 게시글과 같은 방식으로 comment_is_use = "N"
	@Transactional
	public void deleteComment(BoardComment comment) {
		comment.setCommentIsUse("N");
		cr.save(comment);
	}

}
