package com.hitech.lms.domain.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

	private final LessonRepository lr;

	// 목록 - 페이징(10개)
	public Page<Lesson> getList(int page) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("lessonId")));
		return lr.findAll(pageable);
	}

	// 상세 - findById, 없으면 DataNotFoundException
		public Lesson getLesson(Long id) {
			return lr.findById(id)
					.orElseThrow(() -> new DataNotFoundException("해당 정보가 없습니다!"));
	}


}
