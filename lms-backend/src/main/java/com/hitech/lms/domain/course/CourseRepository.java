package com.hitech.lms.domain.course;
 
import java.util.List;
 
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface CourseRepository extends JpaRepository<Course, Long> {
 
	// [추가] 로그인한 교수 본인의 사용중(Y) 강좌 - 수업 등록 드롭다운용
	@EntityGraph(attributePaths = {"subject"})
	List<Course> findByTeacher_UserIdAndCourseIsUseOrderByCourseIdDesc(String userId, String courseIsUse);
}
