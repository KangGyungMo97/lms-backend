package com.hitech.lms.domain.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import com.hitech.lms.domain.course.ClassClock;
import com.hitech.lms.domain.course.Course;
import com.hitech.lms.domain.study.Att;
import com.hitech.lms.domain.study.AttService;
import com.hitech.lms.domain.study.LessonAttRow;
import com.hitech.lms.domain.user.UserService;
import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;

// 강의실 = 강의 하나의 대시보드 (대시보드의 [강의실] 버튼)
//   강의 정보 · 오늘 수업 · 차시 목록 · 과제 · 공지를 한 화면에 (전체 목록은 각 메뉴에서)
//   교수: 담당 교수만 / 학생: 수강 대상(학과 + 학년)만 / 관리자: 모두
@Controller
@RequiredArgsConstructor
public class ClassroomController {

	private static final int LIMIT = 5;    // 과제 · 공지 줄 수

	private final AttService as;

	private final DashboardService ds;

	private final UserService us;

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/course/room/{id}")
	public String room(@PathVariable("id") Long id, Authentication auth, Model model) {
		Course course = as.getCourse(id);
		Users user = us.getUser(auth.getName());

		boolean teacher = hasRole(auth, "ROLE_TEACHER");
		boolean owner = teacher && as.isCourseOwner(course, user);
		boolean student = hasRole(auth, "ROLE_STUDENT");
		if (!(owner || hasRole(auth, "ROLE_ADMIN") || (student && as.isTarget(course, user)))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 강의의 담당 교수 · 수강생만 볼 수 있습니다.");
		}

		model.addAttribute("course", course);
		model.addAttribute("timeLabel", ClassClock.label(course));
		model.addAttribute("isOwner", owner);
		model.addAttribute("isStudent", student);
		model.addAttribute("rosterCount", as.getRoster(course).size());

		// 오늘 수업 (오늘 이 강의 수업이 있을 때만 카드가 나옴)
		model.addAttribute("todayList", as.getTodayClasses(List.of(course), student ? user : null));

		// 차시 목록 + 출석
		List<LessonAttRow> lessonRows = as.getLessonRows(course);
		model.addAttribute("lessonRows", lessonRows);
		long done = lessonRows.stream().filter(r -> !"NONE".equals(r.getState())).count();
		model.addAttribute("doneLessons", done);

		if (student) {
			// 내 출석 { 차시번호 : 상태 } + 내 출석률
			Map<Long, String> myAtt = new HashMap<>();
			for (Att a : as.getMyAtts(user)) {
				if (a.getLesson().getCourse().getCourseId().equals(course.getCourseId())) {
					myAtt.put(a.getLesson().getLessonId(), a.getAttStatus());
				}
			}
			model.addAttribute("myAtt", myAtt);
			long attended = myAtt.values().stream().filter(s -> !"N".equals(s)).count();
			model.addAttribute("myRate", done == 0 ? null : (int) Math.round(attended * 100.0 / done));
			model.addAttribute("homeworkRows", ds.getStudentHomeworks(user, List.of(course)).stream().limit(LIMIT).toList());
		} else {
			// 평균 출석률 (진행한 차시 기준)
			model.addAttribute("avgRate", done == 0 ? null : (int) Math.round(lessonRows.stream()
					.filter(r -> !"NONE".equals(r.getState())).mapToInt(LessonAttRow::getRate).average().orElse(0)));
			model.addAttribute("homeworkRows", ds.getTeacherHomeworks(List.of(course), ds.getRosterMap(List.of(course)))
					.stream().limit(LIMIT).toList());
		}

		model.addAttribute("notices", ds.getNotices());
		return "course/room";
	}

	private boolean hasRole(Authentication auth, String role) {
		return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
	}
}
