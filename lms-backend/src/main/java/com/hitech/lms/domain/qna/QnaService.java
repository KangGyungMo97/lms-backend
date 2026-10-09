package com.hitech.lms.domain.qna;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.hitech.lms.domain.user.Users;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class QnaService {

	private final QnaBoardRepository qr;

	private final AnswerRepository ar;

	// ===== 질문 =====

	// 목록 - 최신순 + 페이징(10개) + 검색  (QuestionService.getList 참고)
	public Page<QnaBoard> getList(int page, String kw) {
		return null;
	}

	// 상세 - findById, 없으면 DataNotFoundException
	public QnaBoard getQna(Long id) {
		return null;
	}

	// 작성 - 작성일 now, 작성자, 수업(lessonId), 상태(답변대기) 넣고 저장
	public QnaBoard create(String title, String content, Long lessonId, Users writer) {
		return null;
	}

	// 수정
	public void modify(QnaBoard qna, String title, String content) {

	}

	// 삭제 - qna_is_use 를 "N" 으로 바꿀지, 진짜 delete 할지 팀 협의
	public void delete(QnaBoard qna) {

	}

	// ===== 답변 =====

	// 답변 찾기 - findById, 없으면 DataNotFoundException
	public Answer getAnswer(Long answerId) {
		return null;
	}

	// 답변 등록 - 작성일 now, 작성자(교수) 넣고 저장 + 질문 상태 변경
	public Answer createAnswer(QnaBoard qna, String content, Users writer) {
		return null;
	}

	// 답변 수정
	public void modifyAnswer(Answer answer, String content) {

	}

	// 답변 삭제
	public void deleteAnswer(Answer answer) {

	}

}
