package com.hitech.lms.domain.study;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.course.Lesson;
import com.hitech.lms.domain.user.Dept;
import com.hitech.lms.domain.user.Users;

public interface AttRepository extends JpaRepository<Att, Long> {

	// 이 학생의 이 수업 출석 기록 (없으면 아직 기록 없음)
	Optional<Att> findByStudentAndLesson(Users student, Lesson lesson);

	// 이 수업에 출석한 학생들 - 먼저 입력한 순서 (교수 화면)
	@EntityGraph(attributePaths = "student")
	List<Att> findByLessonOrderByAttDateAsc(Lesson lesson);

	// 내 출석 기록 - 최근 순 (학생 화면)
	@EntityGraph(attributePaths = { "lesson", "lesson.course" })
	List<Att> findByStudentOrderByAttDateDesc(Users student);

	// ===== 출석부 (교수) =====
	// ※ 공통 Repository(Lesson · Users)를 건드리지 않으려고 여기에 @Query 로 둠

	// 이 과정의 모든 출석 기록 (Att → lesson → course)
	@EntityGraph(attributePaths = { "student", "lesson" })
	List<Att> findByLesson_Course(Course course);

	// 이 과정의 수업 회차 - 오래된 순 (출석부의 가로 칸)
	@Query("select l from Lesson l where l.course = :course order by l.lessonId asc")
	List<Lesson> findLessonsOfCourse(@Param("course") Course course);

	// 수강 대상 학생 - 학과 + 대상 학년, 학번 순 (출석부의 세로 줄 · 수강 인원)
	//   grade 가 null 이면 학과 전체
	// TODO: 수강신청 테이블이 생기면 '이 과정을 신청한 학생' 으로 바꾸기
	@Query("select u from Users u where u.userRole = 'S' and u.dept = :dept "
			+ "and (:grade is null or u.userGrade = :grade) order by u.userId asc")
	List<Users> findStudentsOf(@Param("dept") Dept dept, @Param("grade") String grade);

	// 학생이 듣는 강의 - 같은 학과 과목 중 대상 학년이 내 학년인 것 (대상 학년이 비어 있으면 학과 전체 대상)
	@Query("select c from Course c join fetch c.subject s join fetch c.teacher "
			+ "where c.courseIsUse = 'Y' and s.dept = :dept "
			+ "and (c.courseTarget is null or c.courseTarget = :grade) order by c.courseId asc")
	List<Course> findCoursesOf(@Param("dept") Dept dept, @Param("grade") Integer grade);
}
