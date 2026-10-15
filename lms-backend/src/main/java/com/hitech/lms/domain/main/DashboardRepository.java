package com.hitech.lms.domain.main;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.hitech.lms.domain.board.Notice;
import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.qna.QnaBoard;
import com.hitech.lms.domain.study.AttSession;
import com.hitech.lms.domain.study.Homework;
import com.hitech.lms.domain.study.Submit;
import com.hitech.lms.domain.user.Users;

/**
 * 대시보드 "조회 전용" Repository
 * - 과제 · Q&A · 공지 · 출석 테이블을 읽기만 함
 * - 각 기능의 Repository 를 건드리지 않으려고 따로 만듦 (InfoNoticeRepository 와 같은 방식)
 * - 목록(in :courses)이 비어 있으면 Service 에서 호출하지 않음
 */
public interface DashboardRepository extends Repository<Course, Long> {

	// 이 강의들의 과제 - 마감 빠른 순
	@Query("select h from Homework h join fetch h.lesson l join fetch l.course c "
			+ "where c in :courses order by h.homeworkEndDate asc, h.homeworkId asc")
	List<Homework> findHomeworks(@Param("courses") Collection<Course> courses);

	// 이 과제들의 제출물 전체 (교수 - 제출 · 채점 현황)
	@Query("select s from Submit s where s.homework in :homeworks")
	List<Submit> findSubmits(@Param("homeworks") Collection<Homework> homeworks);

	// 이 과제들의 내 제출물 (학생 - 제출 상태)
	@Query("select s from Submit s where s.student = :student and s.homework in :homeworks")
	List<Submit> findMySubmits(@Param("student") Users student, @Param("homeworks") Collection<Homework> homeworks);

	// 내 강의의 답변 대기(W) 질문 - 최근 순
	@Query("select q from QnaBoard q join fetch q.lesson l join fetch l.course c "
			+ "where q.qnaIsUse = 'Y' and q.qnaStatus = 'W' and c.teacher.userId = :userId "
			+ "order by q.qnaCreateDate desc")
	List<QnaBoard> findWaitingQna(@Param("userId") String userId);

	// 최근 과목 공지
	@Query("select n from Notice n where n.noticeIsUse = 'Y' order by n.noticeId desc")
	List<Notice> findRecentNotices(Pageable pageable);

	// 이 강의들에서 기간 안에 진행한 수업(출석 세션) - 출석률 계산용
	@Query("select s from AttSession s join fetch s.lesson l "
			+ "where l.course in :courses and s.openedAt >= :from and s.openedAt < :to")
	List<AttSession> findSessions(@Param("courses") Collection<Course> courses,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
