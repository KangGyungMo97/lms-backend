package com.hitech.lms.domain.study;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class HomeworkService {

	private final HomeworkRepository hr;

	private final SubmitRepository sr;

	// ===== 과제 =====

	// 목록 - 학생: 듣는 수업의 과제 / 교수: 담당 수업의 과제
	public Page<Homework> getList(int page, Users user) {
		return null;
	}

	// 상세 - findById, 없으면 DataNotFoundException
	public Homework getHomework(Long id) {
		return null;
	}

	// 등록 - lessonId 로 수업 찾아서 넣고 저장
	public void create(Long lessonId, String title, String content, LocalDate startDate, LocalDate endDate) {

	}

	// 수정
	public void modify(Homework homework, String title, String content, LocalDate startDate, LocalDate endDate) {

	}

	// 삭제
	public void delete(Homework homework) {

	}

	// ===== 제출 =====

	// 제출물 찾기 - findById, 없으면 DataNotFoundException
	public Submit getSubmit(Long submitId) {
		return null;
	}

	// 내 제출물 (과제 상세에서 학생용)
	public Submit getMySubmit(Homework homework, Users student) {
		return null;
	}

	// 제출 - 제출일 now, submit_is_done = "Y"
	public void createSubmit(Homework homework, String content, Users student) {

	}

	// 제출 수정
	public void modifySubmit(Submit submit, String content) {

	}

	// 제출 삭제
	public void deleteSubmit(Submit submit) {

	}

	// 제출 현황 - 과제별 제출 목록 (교수)
	public List<Submit> getSubmitList(Homework homework) {
		return null;
	}

	// 채점 - 점수, 피드백, 채점일 now, submit_is_done = "R"
	public void grade(Submit submit, Integer score, String feedback) {

	}

}
