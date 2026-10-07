package com.hitech.lms.domain.course;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** 수업 회차·다시보기 Service
 *  담당: 김경모
 *  TODO: 비즈니스 로직 (목록/상세/등록/수정/삭제 등) */
@Service
@RequiredArgsConstructor
public class LessonService {

	private final LessonRepository lessonRepository;

}
