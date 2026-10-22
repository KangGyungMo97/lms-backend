package com.hitech.lms.domain.qna;

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

import com.hitech.lms.domain.course.Lesson;
import com.hitech.lms.domain.course.LessonRepository;
import com.hitech.lms.domain.user.Users;
import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

/** 수업 Q&A Service (질문 + 답변)
 *  qna_status : W 답변대기 / C 답변완료 (컬럼 길이 1) */
@Service
@RequiredArgsConstructor
public class QnaService {

	private final QnaBoardRepository qr;

	private final AnswerRepository ar;

	private final LessonRepository lr;   // 질문 작성 시 '수업 선택' 목록 (읽기만)

	// ===== 질문 =====

	// 목록 - 최신순 + 페이징(10개) + 검색  (QuestionService.getList 참고)
	public Page<QnaBoard> getList(int page, String kw) {

		page = Math.max(page, 0);                 // ?page=-1 같은 잘못된 값 방지

		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("qnaId"));      // 최신 질문이 위로

		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		return qr.findAllByKeyword(kw, pageable);
	}

	// 상세 - findById, 없거나 삭제(N)된 질문이면 DataNotFoundException
	public QnaBoard getQna(Long id) {

		Optional<QnaBoard> qna = qr.findById(id);

		if (qna.isPresent() && "Y".equals(qna.get().getQnaIsUse())) {
			return qna.get();
		} else {
			throw new DataNotFoundException("질문을 찾을 수 없습니다.");
		}
	}

	// 작성 - 작성일 now, 작성자, 수업(lessonId), 상태(W 답변대기) 넣고 저장
	@Transactional
	public QnaBoard create(String title, String content, Long lessonId, Users writer) {

		Lesson lesson = lr.findById(lessonId)
				.orElseThrow(() -> new DataNotFoundException("수업을 찾을 수 없습니다."));

		QnaBoard qna = new QnaBoard();

		qna.setQnaTitle(title);
		qna.setQnaContent(content);
		qna.setQnaCreateDate(LocalDateTime.now());
		qna.setQnaStatus("W");                    // 답변대기
		qna.setLesson(lesson);
		qna.setWriter(writer);

		qr.save(qna);

		return qna;
	}

	// 수정 (P2)
	public void modify(QnaBoard qna, String title, String content) {

	}

	// 삭제 - qna_is_use 를 "N" 으로 바꿀지, 진짜 delete 할지 팀 협의 (P2)
	public void delete(QnaBoard qna) {

	}

	// 질문 작성 화면의 '수업 선택' 목록 - 최근 수업이 위로
	// TODO: 수강신청 테이블이 생기면 '내가 듣는 수업만' 으로 바꾸기
	public List<Lesson> getLessonList() {
		return lr.findAll(Sort.by(Sort.Direction.DESC, "lessonId"));
	}

	// ===== 답변 =====

	// 이 질문의 답변 목록
	public List<Answer> getAnswerList(QnaBoard qna) {
		return ar.findByQnaBoardAndAnswerIsUseOrderByAnswerIdAsc(qna, "Y");
	}

	// 답변 찾기 - findById, 없으면 DataNotFoundException
	public Answer getAnswer(Long answerId) {

		Optional<Answer> answer = ar.findById(answerId);

		if (answer.isPresent()) {
			return answer.get();
		} else {
			throw new DataNotFoundException("답변을 찾을 수 없습니다.");
		}
	}

	// 답변 등록 - 작성일 now, 작성자(교수) 넣고 저장 + 질문 상태 C(답변완료) 로 변경
	@Transactional
	public Answer createAnswer(QnaBoard qna, String content, Users writer) {

		Answer answer = new Answer();

		answer.setAnswerContent(content);
		answer.setAnswerCreateDate(LocalDateTime.now());
		answer.setQnaBoard(qna);
		answer.setWriter(writer);

		ar.save(answer);

		qna.setQnaStatus("C");                    // 답변완료
		qr.save(qna);

		return answer;
	}

	// 답변 수정 (P2)
	public void modifyAnswer(Answer answer, String content) {

	}

	// 답변 삭제 (P2)
	public void deleteAnswer(Answer answer) {

	}

}
