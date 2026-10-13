package com.hitech.lms.domain.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.user.Dept;
import com.hitech.lms.domain.user.DeptRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.domain.user.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final AdminPostRepository apr;
	private final UsersRepository ur;
	private final DeptRepository dr;
	private final UserStatusLogRepository uslr;

	// ===== 회원 =====

	// 회원 목록 - 페이징 + 검색 (이름, 학번) + 필터 (구분, 학과, 상태)
	public Page<Users> getUserList(int page, String kw, String role, String deptId, String status) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "userCreated"));
		return ur.search(role, deptId, status, kw.trim(), pageable);
	}

	// 통계 카드: 전체 / 학생 / 교수 / 차단·잠금
	public Map<String, Long> getUserStats() {
		return Map.of(
			"total",   ur.count(),
			"student", ur.countByUserRole("S"),
			"teacher", ur.countByUserRole("P"),
			"blocked", ur.countByUserStatusIn(List.of("L", "B"))
		);
	}

	// 상태 변경 (정상 / 잠금 / 차단 / 탈퇴) + 이력 기록
		@Transactional
		public void changeStatus(Users user, String status, String reason,
		                         LocalDate blockStart, LocalDate blockEnd, String adminId) {

			if (!List.of("A", "L", "B", "W").contains(status)) {
				throw new IllegalArgumentException("상태 값이 올바르지 않습니다.");
			}
			if (user.getUserId().equals(adminId)) {
				throw new IllegalArgumentException("본인 계정의 상태는 변경할 수 없습니다.");
			}
			if ("W".equals(user.getUserStatus())) {
				throw new IllegalArgumentException("탈퇴 처리된 계정은 상태를 변경할 수 없습니다.");
			}
			if (status.equals(user.getUserStatus())) {
				throw new IllegalArgumentException("이미 해당 상태입니다.");
			}
			if (reason == null || reason.isBlank()) {
				throw new IllegalArgumentException("사유는 필수 항목입니다.");
			}
			if ("B".equals(status)) {
				if (blockStart == null || blockEnd == null) {
					throw new IllegalArgumentException("차단 기간을 입력해 주세요.");
				}
				if (blockEnd.isBefore(blockStart)) {
					throw new IllegalArgumentException("차단 종료일은 시작일보다 빠를 수 없습니다.");
				}
			} else {
				blockStart = null;
				blockEnd = null;
			}

			UserStatusLog log = new UserStatusLog();
			log.setUser(user);
			log.setBeforeStatus(user.getUserStatus());
			log.setAfterStatus(status);
			log.setReason(reason.trim());
			log.setBlockStart(blockStart);
			log.setBlockEnd(blockEnd);
			log.setChangedAt(LocalDateTime.now());
			log.setChangedBy(adminId);
			uslr.save(log);

			user.setUserStatus(status);
			ur.save(user);
		}

		// 상태 변경 이력 (최신순)
		public List<UserStatusLog> getStatusLogs(Users user) {
			return uslr.findByUserOrderByChangedAtDesc(user);
		}

		// ===== 관리자 게시글 (시스템 공지) =====

		// 목록 - 최신순 + 페이징(10개) + 검색  (QuestionService.getList 참고)
		public Page<AdminPost> getPostList(int page, String kw) {
			return null;
		}

		// 상세 - findById, 없으면 DataNotFoundException
		public AdminPost getPost(Long id) {
			return null;
		}

		// 조회수 +1
		public void increaseViewCount(AdminPost adminPost) {

		}

		// ===== 학과 =====

		// 학과 목록
		public List<Dept> getDeptList() {
			return dr.findAll();
		}

	}
