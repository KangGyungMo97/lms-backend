package com.hitech.lms.domain.course;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectService {

	private final SubjectRepository sr;

	// 목록 - 페이징(10개) + 검색  (QuestionService.getList 참고)
	public Page<Subject> getList(int page, String kw) {
		return null;
	}

	// 상세 - findById, 없으면 DataNotFoundException
	public Subject getSubject(String id) {
		return null;
	}

	// 등록 - deptId 로 학과 찾아서 넣고 저장
	public void create(String subjectId, String subjectName, String deptId) {

	}

	// 수정
	public void modify(Subject subject, String subjectName, String deptId) {

	}

	// 삭제
	public void delete(Subject subject) {

	}

}
