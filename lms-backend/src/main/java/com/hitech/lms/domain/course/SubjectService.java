package com.hitech.lms.domain.course;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.user.Dept;
import com.hitech.lms.domain.user.DeptRepository;
import com.hitech.lms.global.error.DataNotFoundException;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectService {

	private final SubjectRepository sr;
	private final DeptRepository dr;

	// 목록 - 페이징(10개) + 검색
	@Transactional(readOnly = true)
	public Page<Subject> getList(int page, String kw) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by("subjectId").ascending());
		return sr.findAll(search(kw), pageable);
	}

	// 상세 - findById, 없으면 DataNotFoundException
	@Transactional(readOnly = true)
	public Subject getSubject(String id) {
		Optional<Subject> result = sr.findById(id);
		if (result.isPresent()) {
			return result.get();
		} else {
			throw new DataNotFoundException("과목을 찾을 수 없습니다!");
		}
	}

	private Specification<Subject> search(String kw) {
		return (root, query, cb) -> {
			if (kw == null || kw.isBlank()) {
				return cb.conjunction();
			}
			Join<Subject, Dept> d = root.join("dept", JoinType.LEFT);
			String like = "%" + kw + "%";
			return cb.or(
				cb.like(root.get("subjectName"), like),
				cb.like(root.get("subjectId"), like),
				cb.like(d.get("deptName"), like)
			);
		};
	}

	// 등록 - deptId 로 학과 찾아서 넣고 저장
	@Transactional
	public void create(String subjectId, String subjectName, String deptId) {
		if (sr.existsById(subjectId)) {
			throw new IllegalArgumentException("이미 존재하는 과목 코드입니다.");
		}
		Subject subject = new Subject();
		subject.setSubjectId(subjectId);
		subject.setSubjectName(subjectName);
		subject.setDept(findDept(deptId));
		sr.save(subject);
	}

	// 수정
	@Transactional
	public void modify(Subject subject, String subjectName, String deptId) {
		subject.setSubjectName(subjectName);
		subject.setDept(findDept(deptId));
		sr.save(subject);
	}

	// 삭제 - [수정] flush 로 FK 위반을 이 메서드 안에서 바로 발생시켜 컨트롤러가 잡을 수 있게 함
	@Transactional
	public void delete(Subject subject) {
		sr.delete(subject);
		sr.flush();
	}

	private Dept findDept(String deptId) {
		Optional<Dept> result = dr.findById(deptId);
		if (result.isPresent()) {
			return result.get();
		} else {
			throw new DataNotFoundException("학과를 찾을수 없습니다!");
		}
	}
	
	public List<Subject> getList() {
	    return sr.findAll();
	}

}