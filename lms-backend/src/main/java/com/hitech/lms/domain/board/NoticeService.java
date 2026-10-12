package com.hitech.lms.domain.board;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hitech.lms.DataNotFoundException;
import com.hitech.lms.domain.user.Users;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

	private final NoticeRepository nr;

	// 목록 - 최신순 + 페이징(10개) + 검색  (QuestionService.getList 참고)
	public Page<Notice> getList(int page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("noticeCreateDate"));
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		return nr.findAllByKeyword(kw == null ? "" : kw, pageable);
	}
	

	// 상세 - findById, 없으면 DataNotFoundException
	public Notice getNotice(Long id, HttpServletRequest request, HttpServletResponse response) {
		Optional<Notice> on = nr.findById(id);
		if(on.isPresent()) {
			Notice notice = on.get();
			Cookie oldCookie=null;
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
				// 이미 읽은 게시글 목록 쿠키가 존재하는 경우
				if (!oldCookie.getValue().contains("[" + id.toString() + "]")) {
					// 현재 게시글 ID가 쿠키에 없다면 조회수 증가 후 쿠키에 ID 추가
					notice.setNoticeViewCount(notice.getNoticeViewCount() + 1);
					oldCookie.setValue(oldCookie.getValue() + "_[" + id + "]");
					oldCookie.setPath("/");
					oldCookie.setMaxAge(60 * 60 * 24); // 쿠키 유효기간: 하루(24시간)
					response.addCookie(oldCookie);
				}
			} else {
				// 최초로 게시글 유입 쿠키가 생성되는 경우
				notice.setNoticeViewCount(notice.getNoticeViewCount() + 1);
				Cookie newCookie = new Cookie("noticeViewCount", "[" + id + "]");
				newCookie.setPath("/");
				newCookie.setMaxAge(60 * 60 * 24); // 하루
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

	// 작성 - 작성일 now, 작성자 넣고 저장
	public void create(String title, String content, Users writer) {
		Notice notice = new Notice();
		notice.setNoticeTitle(title);
		notice.setNoticeContent(content);
		notice.setNoticeCreateDate(LocalDate.now());
		notice.setNoticeUpdateDate(LocalDate.now());  // nullable=false라서 필요
		notice.setWriter(writer);                    
		nr.save(notice);
	}

	// 수정 - 수정일 now
	public void modify(Notice notice, String title, String content) {
		notice.setNoticeTitle(title);
		notice.setNoticeContent(content);
		notice.setNoticeUpdateDate(LocalDate.now());
		nr.save(notice);
	}

	// 삭제 - notice_is_use 를 "N" 으로 바꿀지, 진짜 delete 할지 팀 협의
	public void delete(Notice notice) {
		notice.setNoticeIsUse("N");
		nr.save(notice);
	}

}