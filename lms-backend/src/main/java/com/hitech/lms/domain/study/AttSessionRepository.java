package com.hitech.lms.domain.study;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.course.Lesson;

public interface AttSessionRepository extends JpaRepository<AttSession, Long> {

	// 이 수업의 출석 세션 (없으면 아직 시작 안 한 것)
	Optional<AttSession> findByLesson(Lesson lesson);

	// 여러 수업의 세션 한 번에 (교수 목록 - 수업마다 쿼리 날리지 않으려고)
	List<AttSession> findByLessonIn(Collection<Lesson> lessons);

	// 이 과정의 세션 (출석부에서 '진행한 수업' 구분용)
	List<AttSession> findByLesson_Course(Course course);

	// 지금 열려 있는 세션 (마감 시각이 지금보다 뒤) - 학생 화면
	@EntityGraph(attributePaths = { "lesson", "lesson.course", "lesson.course.teacher" })
	List<AttSession> findByExpiresAtAfterOrderByOpenedAtDesc(LocalDateTime now);
}
