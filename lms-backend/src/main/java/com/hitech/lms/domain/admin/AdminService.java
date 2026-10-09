package com.hitech.lms.domain.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

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

	// ===== 회원 =====

	// 회원 목록 - 페이징 + 검색 (이름, 학번)
	public Page<Users> getUserList(int page, String kw) {
		return null;
	}

	// 상태 변경 - user_status 코드값은 팀 협의
	public void changeStatus(Users user, String status) {

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
		return null;
	}

}
