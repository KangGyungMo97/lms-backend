package com.hitech.lms.domain.course;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonService {

	private final LessonRepository lr;

	// 목록 - 페이징(10개)
	public Page<Lesson> getList(int page) {
		return null;
	}

	// 상세 - findById, 없으면 DataNotFoundException
	public Lesson getLesson(Long id) {
		return null;
	}

	// 다시보기 목록 - 내가 듣는 / 담당하는 수업 회차
	public Page<Lesson> getReplayList(int page, Users user) {
		return null;
	}

	// 이어보기 위치 저장
	// ERD 에 이어보기(재생 위치) 테이블이 없음 -> 엔티티 추가 후 맞추기
	public void saveProgress(Lesson lesson, Users student, Integer position) {

	}

}
