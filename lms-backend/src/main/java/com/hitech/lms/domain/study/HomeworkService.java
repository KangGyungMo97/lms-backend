package com.hitech.lms.domain.study;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hitech.lms.domain.course.Lesson;
import com.hitech.lms.domain.course.LessonRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class HomeworkService {

	private final HomeworkRepository hr;

	private final SubmitRepository sr;

	private final LessonRepository lr;

	// ===== 과제 =====

	// 목록 - 학생: 듣는 수업의 과제 / 교수: 담당 수업의 과제
	public Page<Homework> getList(int page, Users user) {
		page = Math.max(page, 0);                     // ?page=-1 같은 잘못된 값 방지
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("homeworkId"));     // 최근에 낸 과제가 위로
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));

		if ("P".equals(user.getUserRole())) {         // 교수 → 내가 담당하는 수업의 과제만
			return hr.findByLesson_Course_Teacher_UserId(user.getUserId(), pageable);
		}
		// 학생 → 지금은 전체 과제
		// TODO: ERD 에 수강신청(학생-과목) 테이블이 없음 → 생기면 '내가 듣는 과목의 과제만' 으로 바꾸기
		return hr.findAll(pageable);
	}

	// 상세 - findById, 없으면 404
	// TODO: global 에 DataNotFoundException 이 생기면 그걸로 바꾸기
	public Homework getHomework(Long id) {
		return hr.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));
	}

	// 등록 - lessonId 로 수업 찾아서 넣고 저장
	@Transactional
	public void create(Long lessonId, String title, String content, LocalDate startDate, LocalDate endDate) {
		Lesson lesson = lr.findById(lessonId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수업을 찾을 수 없습니다."));

		Homework homework = new Homework();
		homework.setLesson(lesson);
		homework.setHomeworkTitle(title);
		homework.setHomeworkContent(content);
		homework.setHomeworkStartDate(startDate);
		homework.setHomeworkEndDate(endDate);
		hr.save(homework);
	}

	// 수정
	public void modify(Homework homework, String title, String content, LocalDate startDate, LocalDate endDate) {

	}

	// 삭제
	public void delete(Homework homework) {

	}

	// 등록 화면의 '수업 선택' 목록 - 교수가 담당하는 수업 회차
	public List<Lesson> getMyLessons(Users teacher) {
		return lr.findByCourse_Teacher_UserIdOrderByLessonIdDesc(teacher.getUserId());
	}

	// 이 과제를 낸 교수인지 (과제 → 수업 → 과목 → 담당 교수)
	public boolean isOwner(Homework homework, Users user) {
		return homework.getLesson().getCourse().getTeacher().getUserId().equals(user.getUserId());
	}

	// ===== 제출 =====

	// 제출물 찾기 - findById, 없으면 DataNotFoundException
	public Submit getSubmit(Long submitId) {

		Optional<Submit> submit = sr.findById(submitId);

		if (submit.isPresent()) {
			return submit.get();
		} else {
			throw new DataNotFoundException("제출물을 찾을 수 없습니다.");
		}
	}

	// 내 제출물 (과제 상세에서 학생용) - 아직 안 냈으면 null
	public Submit getMySubmit(Homework homework, Users student) {
		return sr.findByHomeworkAndStudent(homework, student).orElse(null);
	}

	// 과제 목록의 '제출 여부' 칸 - { 과제번호 : 제출상태(Y 제출완료 / R 채점완료 ...) }
	public Map<Long, String> getMySubmitStatus(Users student) {
		Map<Long, String> status = new HashMap<>();
		for (Submit submit : sr.findByStudent(student)) {
			status.put(submit.getHomework().getHomeworkId(), submit.getSubmitIsDone());
		}
		return status;
	}

	// 제출 - 제출일 now, submit_is_done = "Y"
	@Transactional
	public void createSubmit(Homework homework, String content, Users student) {

		Submit submit = new Submit();

		submit.setHomework(homework);
		submit.setStudent(student);
		submit.setSubmitContent(content);
		submit.setSubmitDate(LocalDateTime.now());
		submit.setSubmitIsDone("Y");          // 제출완료

		sr.save(submit);
	}

	// 제출 수정
	public void modifySubmit(Submit submit, String content) {

	}

	// 제출 삭제
	public void deleteSubmit(Submit submit) {

	}

	// 제출 현황 - 과제별 제출 목록 (교수), 먼저 낸 순서
	public List<Submit> getSubmitList(Homework homework) {
		return sr.findByHomeworkOrderBySubmitDateAsc(homework);
	}

	// 채점 - 점수, 피드백, 채점일 now, submit_is_done = "R"
	@Transactional
	public void grade(Submit submit, Integer score, String feedback) {

		submit.setSubmitScore(score);
		submit.setSubmitFeedback(feedback);
		submit.setSubmitScoreDate(LocalDate.now());
		submit.setSubmitIsDone("R");          // 채점완료

		sr.save(submit);
	}

}
