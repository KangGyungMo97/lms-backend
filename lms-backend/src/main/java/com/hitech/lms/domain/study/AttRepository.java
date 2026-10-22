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
	// ===== 출석부 및 대시보드 연동용 쿼리 개정 =====

		// 수강 대상 학생 목록 조회: 해당 학과의 학생("S")이면서, 학년 정보가 targets 리스트(일반 학년 또는 하이테크 과정 9)에 포함되는 경우
		@Query("select u from Users u where u.userRole = 'S' and u.dept = :dept "
				+ "and u.userGrade in :targets order by u.userId asc")
		List<Users> findStudentsOfDeptAndTargets(@Param("dept") Dept dept, @Param("targets") List<String> targets);

		// 학생이 듣는 강의 목록 조회: 해당 학과의 과목이면서, 과정 대상(courseTarget)이 targets 리스트(일반 학년 또는 하이테크 과정 9)에 포함되거나 전체 대상(null)인 경우
		@Query("select c from Course c join fetch c.subject s join fetch c.teacher "
				+ "where c.courseIsUse = 'Y' and s.dept = :dept " // c.dept -> s.dept 로 올바르게 수정
				+ "and (c.courseTarget is null or c.courseTarget in :targets) order by c.courseId asc")
		List<Course> findCoursesOfDeptAndTargets(@Param("dept") Dept dept, @Param("targets") List<Integer> targets);}
