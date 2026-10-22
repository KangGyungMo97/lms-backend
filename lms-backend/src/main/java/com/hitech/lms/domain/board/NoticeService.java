package com.hitech.lms.domain.board;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.domain.course.Subject;
import com.hitech.lms.domain.course.SubjectRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.global.error.DataNotFoundException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

	private final NoticeRepository nr;
	private final SubjectRepository sr;

	// 목록 - 최신순 + 페이징(10개) + 검색
	public Page<Notice> getList(int page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("noticeCreateDate"));
		sorts.add(Sort.Order.desc("noticeId"));
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		return nr.findAllByKeyword(kw == null ? "" : kw, pageable);
	}
	
	// 상세 - 조회수 처리 포함
	public Notice getNotice(Long id, HttpServletRequest request, HttpServletResponse response) {
		Optional<Notice> on = nr.findById(id);
		if(on.isPresent()) {
			Notice notice = on.get();
			Cookie oldCookie = null;
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("noticeViewCount")) {
						oldCookie = cookie;
						break;
					}
				}
			}
			if (oldCookie != null) {
				if (!oldCookie.getValue().contains("[" + id.toString() + "]")) {
					notice.setNoticeViewCount(notice.getNoticeViewCount() + 1);
					oldCookie.setValue(oldCookie.getValue() + "_[" + id + "]");
					oldCookie.setPath("/");
					oldCookie.setMaxAge(60 * 60 * 24); 
					response.addCookie(oldCookie);
				}
			} else {
				notice.setNoticeViewCount(notice.getNoticeViewCount() + 1);
				Cookie newCookie = new Cookie("noticeViewCount", "[" + id + "]");
				newCookie.setPath("/");
				newCookie.setMaxAge(60 * 60 * 24); 
				response.addCookie(newCookie);
			}
			nr.save(notice);
			return notice;
		} else {
			throw new DataNotFoundException("해당 정보가 없습니다!");
		}
	}
	
	@Transactional(readOnly=true)
	public Notice getNotice(Long id) {
		return this.getNoticeWithoutView(id);
	}
	
	@Transactional(readOnly=true)
	public Notice getNoticeWithoutView(Long id) {
		Optional<Notice> on = nr.findById(id);
		if(on.isPresent()) {
			return on.get();
		} else {
			throw new DataNotFoundException("해당정보가 없습니다!");
		}
	}

	// 📌 [수정 완] 첫 번째 파라미터를 Long에서 String subjectId로 변경하여 sr.findById와의 충돌 해결
	@Transactional
	public void create(String subjectId, String title, String content, Users writer) {
	    Notice notice = new Notice();
	    notice.setNoticeTitle(title);
	    notice.setNoticeContent(content);
	    notice.setWriter(writer);
	    notice.setNoticeViewCount(0); 
	    notice.setNoticeCreateDate(LocalDate.now()); 
	    
	    // 📌 이 줄이 들어가 있어야 데이터베이스에서 500 NULL 에러를 뱉지 않습니다!
	    notice.setNoticeUpdateDate(LocalDate.now()); 
	    
	    notice.setNoticeIsUse("Y");
	    
	    if (subjectId != null && !subjectId.isBlank()) {
	        Subject subject = sr.findById(subjectId)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다."));
	        notice.setSubject(subject);
	    }
	    nr.save(notice);
	}

	// 📌 [수정 완] 첫 번째 파라미터를 Long에서 String subjectId로 변경
	@Transactional
	public void modify(Notice notice, String subjectId, String title, String content) {
		notice.setNoticeTitle(title);
		notice.setNoticeContent(content);
		notice.setNoticeUpdateDate(LocalDate.now()); 

		if (subjectId != null && !subjectId.isBlank()) {
			Subject subject = sr.findById(subjectId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다."));
			notice.setSubject(subject);
		} else {
			notice.setSubject(null);
		}
		
		nr.save(notice); 
	}

	// 삭제
	@Transactional
	public void delete(Notice notice) {
		notice.setNoticeIsUse("N");
		nr.save(notice);
	}
}
