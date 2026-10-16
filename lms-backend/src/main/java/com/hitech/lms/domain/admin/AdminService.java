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
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.DeptRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.domain.user.UsersRepository;
import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final AdminPostRepository apr;
	private final UsersRepository ur;
	private final DeptRepository dr;
	private final UserStatusLogRepository uslr;
	private final UserService us;

	// ===== 회원 =====

	// 회원 목록 - 페이징 + 검색 (이름, 학번) + 필터 (구분, 학과, 상태)
	public Page<Users> getUserList(int page, String kw, String role, String deptId, String status) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "userCreated"));
		return ur.search(role, deptId, status, kw.trim(), pageable);
	}

	// 계정 등록 - 중복이면 IllegalStateException(필드명:메시지) 으로 예외 처리
	@Transactional
	public Users createUser(UserCreateForm f) {
		String userId = f.getUserId().trim();
		String email  = f.getUserEmail().trim();
		
		if (ur.existsById(userId)) {
			throw new IllegalStateException("userId:이미 등록된 학번/사번입니다.");
		}
		if (ur.existsByUserEmail(email)) {
			throw new IllegalStateException("userEmail:이미 사용 중인 이메일입니다.");
		}
		if (!List.of("S", "P", "A").contains(f.getUserRole())) {
			throw new IllegalStateException("userRole:구분 값이 올바르지 않습니다.");
		}
		
		// 전화번호 포맷 정규화 반영
		String normalizedTel = normalizeTel(f.getUserTel());

		// 초기 비밀번호는 생년월일(f.getUserBday())로 설정하여 전달
		Users user = us.create(
				userId, 
				f.getUserName().trim(), 
				f.getUserBday(), 
				email,
				normalizedTel, 
				f.getUserBday(), 
				f.getDeptId(), 
				f.getUserRole()
		);

		// 학년은 학생만 (교수 · 관리자 · 학년 없는 과정은 NULL)
		String grade = f.getUserGrade();
		user.setUserGrade("S".equals(f.getUserRole()) && List.of("1", "2").contains(grade) ? grade : null);
		
		return ur.save(user);
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

	// 목록 - 최신순 + 페이징(10개) + 검색(제목 · 내용)
	public Page<AdminPost> getPostList(int page, String kw) {
		page = Math.max(page, 0);
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "adminPostId"));
		return apr.findPostList(kw.trim(), pageable);
	}

	// 한 개 찾기 - 없거나 삭제(N)된 글이면 DataNotFoundException(404)
	public AdminPost getPost(Long id) {
		AdminPost post = apr.findById(id)
				.orElseThrow(() -> new DataNotFoundException("게시글을 찾을 수 없습니다."));
		if (!"Y".equals(post.getAdminPostIsUse())) {
			throw new DataNotFoundException("게시글을 찾을 수 없습니다.");
		}
		return post;
	}

	// 조회수 +1
	@Transactional
	public void increaseViewCount(AdminPost adminPost) {
		adminPost.setAdminPostView(adminPost.getAdminPostView() + 1);
		apr.save(adminPost);
	}

	// 등록 - 작성자 = 로그인한 관리자
	@Transactional
	public AdminPost createPost(String title, String content, String adminId) {
		AdminPost post = new AdminPost();
		post.setAdminPostTitle(title.trim());
		post.setAdminPostContent(content);
		post.setAdminPostCreateDate(LocalDate.now());
		post.setAdminPostUpdateDate(LocalDate.now());
		post.setWriter(us.getUser(adminId));
		return apr.save(post);
	}

	// 수정 - 수정일 now
	@Transactional
	public void modifyPost(AdminPost post, String title, String content) {
		post.setAdminPostTitle(title.trim());
		post.setAdminPostContent(content);
		post.setAdminPostUpdateDate(LocalDate.now());
		apr.save(post);
	}

	// 삭제 - 자유게시판과 같은 방식 (admin_post_is_use = 'N')
	@Transactional
	public void deletePost(AdminPost post) {
		post.setAdminPostIsUse("N");
		apr.save(post);
	}

	// ===== 학과 및 검증 유틸 =====

	// 학과 목록
	public List<Dept> getDeptList() {
		return dr.findAll();
	}
	
	// 학번/사번 중복 확인
	public boolean isUserIdTaken(String userId) {
		return ur.existsById(userId);
	}

	// 이메일 중복 확인
	public boolean isUserEmailTaken(String email) {
		return ur.existsByUserEmail(email.trim());
	}

	// 전화번호 하이픈 자동 변환기
	private String normalizeTel(String tel) {
		if (tel == null) return null;
		String d = tel.replaceAll("\\D", "");
		if (d.length() == 11) return d.substring(0, 3) + "-" + d.substring(3, 7) + "-" + d.substring(7);
		if (d.length() == 10) return d.substring(0, 3) + "-" + d.substring(3, 6) + "-" + d.substring(6);
		return tel;
	}
}
