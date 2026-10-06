package com.hitech.lms.domain.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Timetable {

	/*학생별 시간표라면 user_id 추가 필요 → 팀 확인 */
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long timetableId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;
}
