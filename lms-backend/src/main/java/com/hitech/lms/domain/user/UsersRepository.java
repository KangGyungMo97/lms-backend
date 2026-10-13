package com.hitech.lms.domain.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsersRepository extends JpaRepository<Users, String> {
	// 아이디 찾기용
	List<Users> findByUserNameAndUserBdayAndUserRole(String userName, String userBday, String userRole);
	// 통계 카드용
	long countByUserRole(String userRole);
	long countByUserStatusIn(List<String> statuses);
	// 관리자 회원 목록: 구분 / 학과 / 상태 / 검색어(이름, 학번·사번) - 빈 문자열이면 조건 무시
		@EntityGraph(attributePaths = "dept")   // 학과를 한 번에 가져와서 N+1 방지
		@Query("select u from Users u " +
		       "where (:role = '' or u.userRole = :role) " +
		       "and (:deptId = '' or u.dept.deptId = :deptId) " +
		       "and (:status = '' or u.userStatus = :status) " +
		       "and (:kw = '' or u.userName like concat('%', :kw, '%') " +
		       "              or u.userId   like concat('%', :kw, '%'))")
		Page<Users> search(@Param("role") String role,
		                   @Param("deptId") String deptId,
		                   @Param("status") String status,
		                   @Param("kw") String kw,
		                   Pageable pageable); 
	}

