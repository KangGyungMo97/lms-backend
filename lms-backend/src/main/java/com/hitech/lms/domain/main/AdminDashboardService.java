package com.hitech.lms.domain.main;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.admin.AdminPost;
import com.hitech.lms.domain.admin.AdminPostRepository;
import com.hitech.lms.domain.admin.AdminService;
import com.hitech.lms.domain.board.Board;
import com.hitech.lms.domain.board.BoardCommentRepository;
import com.hitech.lms.domain.board.BoardRepository;
import com.hitech.lms.domain.course.CourseRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.domain.user.UsersRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 대시보드 데이터 (홍승훈) - 학생 · 교수 대시보드(DashboardService)와 분리
 * - 보여 주기만 함 (등록 · 수정은 각 관리 화면으로 링크)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

	private final AdminService as;              // 회원 통계 (getUserStats) 재사용
	private final UsersRepository ur;
	private final AdminPostRepository apr;
	private final BoardRepository br;
	private final BoardCommentRepository bcr;
	private final CourseRepository cr;

	// ① 요약 카드: 전체 / 학생 / 교수 / 차단·잠금 + 오늘 등록 + 개설 강의
	public Map<String, Long> getAdminStats() {
		Map<String, Long> stats = new HashMap<>(as.getUserStats());
		stats.put("today", ur.countByUserCreatedGreaterThanEqual(LocalDate.now().atStartOfDay()));
		stats.put("course", cr.countByCourseIsUse("Y"));
		return stats;
	}

	// ③ 최근 등록 계정 5명
	public List<Users> getRecentUsers() {
		return ur.findTop5ByOrderByUserCreatedDesc();
	}

	// ⑤ 최근 시스템 공지 5개
	public List<AdminPost> getRecentPosts() {
		return apr.findTop5ByAdminPostIsUseOrderByAdminPostIdDesc("Y");
	}

	// ⑥ 자유게시판 최근 글 5개
	public List<Board> getRecentBoards() {
		return br.findTop5ByBoardIsUseOrderByBoardIdDesc("Y");
	}

	// ⑥ 위 글들의 댓글 수 { 게시글ID : 댓글 수 } (댓글 없는 글은 빠짐)
	public Map<Long, Long> getCommentCounts(List<Board> boards) {
		Map<Long, Long> counts = new HashMap<>();
		List<Long> ids = boards.stream().map(Board::getBoardId).toList();
		if (ids.isEmpty()) return counts;
		for (Object[] row : bcr.countByBoardIds(ids)) {
			counts.put((Long) row[0], (Long) row[1]);
		}
		return counts;
	}
}
