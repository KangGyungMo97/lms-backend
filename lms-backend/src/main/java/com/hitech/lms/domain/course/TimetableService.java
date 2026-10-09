package com.hitech.lms.domain.course;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimetableService {

	private final TimetableRepository tr;

	// 내 시간표 - 학생: 수강 강의 / 교수: 담당 강의 (course.teacher)
	public List<Timetable> getList(Users user) {
		return null;
	}

}
