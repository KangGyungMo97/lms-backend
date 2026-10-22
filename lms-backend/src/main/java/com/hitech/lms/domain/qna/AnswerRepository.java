package com.hitech.lms.domain.qna;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

	// 이 질문의 답변 목록 - 사용 중(Y)인 것만, 먼저 단 순서
	List<Answer> findByQnaBoardAndAnswerIsUseOrderByAnswerIdAsc(QnaBoard qnaBoard, String answerIsUse);

}
