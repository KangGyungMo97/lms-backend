package com.hitech.lms.domain.admin;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.user.Dept;
import com.hitech.lms.domain.user.SmsService;
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
	private final SmsService sms;                     // 임시 비밀번호 문자 발송
	private final SecureRandom random = new SecureRandom();

	// ===== 회원 =====

	// 회원 목록 - 페이징 + 검색 (이름, 학번) + 필터 (구분, 학과, 상태)
	public Page<Users> getUserList(int page, String kw, String role, String deptId, String status) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "userCreated"));
		return ur.search(role, deptId, status, kw.trim(), pageable);
	}

	// 학과는 스마트소프트웨어과 하나로 고정 (팀 결정)
	public static final String DEPT_ID = "D001";

	// 계정 등록 (홍승훈) - 입력 오류는 IllegalStateException("필드명:메시지") 으로 알려 줌
	@Transactional
	public Users createUser(UserCreateForm f) {
		String userId = f.getUserId().trim();
		String email  = f.getUserEmail().trim();
		String role   = f.getUserRole();
		String grade  = f.getUserGrade();

		if (!f.isTempPwIssued()) {
			throw new IllegalStateException("tempPwIssued:[임시 비밀번호 발급] 버튼을 눌러 주세요.");
		}
		if (!List.of("S", "P", "A").contains(role)) {
			throw new IllegalStateException("userRole:구분 값이 올바르지 않습니다.");
		}
		if ("S".equals(role) && !List.of("1", "2").contains(grade)) {
			throw new IllegalStateException("userGrade:학생은 학년(1 · 2학년)을 선택해 주세요.");
		}
		if (ur.existsById(userId)) {
			throw new IllegalStateException("userId:이미 등록된 학번/사번입니다.");
		}
		if (ur.existsByUserEmail(email)) {
			throw new IllegalStateException("userEmail:이미 사용 중인 이메일입니다.");
		}
		// 임시 비밀번호: 서버가 임의로 만들고, 암호화해서 저장 → 관리자는 알 수 없음
		String tempPw = makeTempPassword();
		Users user = us.create(userId, f.getUserName().trim(), tempPw, email,
				f.getUserTel().trim(), f.getUserBday(), DEPT_ID, role);

		// 학년은 학생만 (교수 · 관리자는 NULL)
		user.setUserGrade("S".equals(role) ? grade : null);
		ur.save(user);

		// 계정 주인의 휴대폰으로만 임시 비밀번호 발송 (개발 중에는 LogSmsService 가 서버 콘솔에 출력)
		sms.send(user.getUserTel(), "[하이테크 LMS] " + user.getUserName() + "님 계정이 등록되었습니다. "
				+ "아이디: " + user.getUserId() + " / 임시 비밀번호: " + tempPw
				+ " (로그인 후 비밀번호 찾기에서 새 비밀번호로 바꿔 주세요)");
		return user;
	}

	// 임시 비밀번호 만들기 - 영문 대소문자 + 숫자 + 특수문자 섞어서 10자 (헷갈리는 0 O 1 l I 제외)
	private String makeTempPassword() {
		String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ", lower = "abcdefghijkmnopqrstuvwxyz",
				digit = "23456789", special = "!@#$%^&*";
		String all = upper + lower + digit + special;
		List<Character> chars = new ArrayList<>();
		chars.add(upper.charAt(random.nextInt(upper.length())));      // 종류마다 최소 1개
		chars.add(lower.charAt(random.nextInt(lower.length())));
		chars.add(digit.charAt(random.nextInt(digit.length())));
		chars.add(special.charAt(random.nextInt(special.length())));
		while (chars.size() < 10) chars.add(all.charAt(random.nextInt(all.length())));
		Collections.shuffle(chars, random);
		StringBuilder sb = new StringBuilder();
		chars.forEach(sb::append);
		return sb.toString();
	}

	// 고정 학과 이름 (화면 표시용)
	public String getFixedDeptName() {
		return dr.findById(DEPT_ID).map(Dept::getDeptName).orElse("스마트소프트웨어과");
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

		// ===== 관리자 게시글 (시스템 공지) ===== (홍승훈)
		// 등록한 글은 정보마당 > 시스템 공지(/info/notice/list)에 바로 보임

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

		// 삭제 - 자유게시판과 같은 방식 (admin_post_is_use = 'N', 목록 · 정보마당에서 안 보임)
		@Transactional
		public void deletePost(AdminPost post) {
			post.setAdminPostIsUse("N");
			apr.save(post);
		}

		// ===== 학과 =====

		// 학과 목록
		public List<Dept> getDeptList() {
			return dr.findAll();
		}

	}
