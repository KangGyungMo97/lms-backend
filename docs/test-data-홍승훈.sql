-- 자유게시판 테스트 데이터 (홍승훈)
-- 먼저 구글 드라이브 test-data.sql 을 실행해서 users 가 있어야 함
-- 작성자는 users 테이블의 학생(S) / 교수(P) 중 첫 번째 계정을 사용
-- ※ 학생(S) · 교수(P) 계정이 하나도 없으면 user_id 가 NULL 이 되어 INSERT 가 실패함

INSERT INTO board (board_title, board_content, board_create_date, board_update_date, board_view_count, board_is_use, user_id)
VALUES ('자유게시판 첫 글입니다', '자유게시판 테스트용 글입니다. 상세 화면에서 내용이 잘 보이는지 확인해 주세요.', CURRENT_DATE, CURRENT_DATE, 0, 'Y',
        (SELECT user_id FROM users WHERE user_role = 'S' ORDER BY user_id LIMIT 1));

INSERT INTO board (board_title, board_content, board_create_date, board_update_date, board_view_count, board_is_use, user_id)
VALUES ('SQL 스터디 같이 하실 분 구해요', '데이터베이스 수업 복습 겸 주 2회 SQL 문제 풀이 스터디를 하려고 합니다.', CURRENT_DATE, CURRENT_DATE, 3, 'Y',
        (SELECT user_id FROM users WHERE user_role = 'S' ORDER BY user_id LIMIT 1));

INSERT INTO board (board_title, board_content, board_create_date, board_update_date, board_view_count, board_is_use, user_id)
VALUES ('다음 주 실습실 이용 안내', '다음 주 실습실은 오후 6시까지 이용 가능합니다.', CURRENT_DATE, CURRENT_DATE, 10, 'Y',
        (SELECT user_id FROM users WHERE user_role = 'P' ORDER BY user_id LIMIT 1));

-- 삭제(N) 처리된 글 → 목록 · 상세에 안 보여야 정상
INSERT INTO board (board_title, board_content, board_create_date, board_update_date, board_view_count, board_is_use, user_id)
VALUES ('삭제된 글 (보이면 안 됨)', '이 글은 목록에 나오면 안 됩니다.', CURRENT_DATE, CURRENT_DATE, 0, 'N',
        (SELECT user_id FROM users WHERE user_role = 'S' ORDER BY user_id LIMIT 1));

-- 페이징 확인용 글 12개
INSERT INTO board (board_title, board_content, board_create_date, board_update_date, board_view_count, board_is_use, user_id)
SELECT CONCAT('페이징 테스트 글 ', x), '페이징 확인용 글입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y',
       (SELECT user_id FROM users WHERE user_role = 'S' ORDER BY user_id LIMIT 1)
FROM (SELECT 1 AS x UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
      UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12) t;
