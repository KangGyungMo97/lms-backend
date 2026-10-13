-- 자유게시판 댓글 테스트 데이터 (홍승훈)
-- 먼저 users 와 test-data-홍승훈.sql(게시글)이 들어가 있어야 함
-- board_comment 테이블은 서버를 한 번 켜면 자동으로 만들어짐 (ddl-auto=update)
-- 가장 최근 게시글(삭제 안 된 것)에 댓글 3개 + 삭제된 댓글 1개

INSERT INTO board_comment (comment_content, comment_create_date, comment_is_use, board_id, user_id)
VALUES ('좋은 글 감사합니다!', CURRENT_TIMESTAMP, 'Y',
        (SELECT MAX(board_id) FROM board WHERE board_is_use = 'Y'),
        (SELECT user_id FROM users WHERE user_role = 'S' ORDER BY user_id LIMIT 1));

INSERT INTO board_comment (comment_content, comment_create_date, comment_is_use, board_id, user_id)
VALUES ('저도 참여하고 싶어요.', CURRENT_TIMESTAMP, 'Y',
        (SELECT MAX(board_id) FROM board WHERE board_is_use = 'Y'),
        (SELECT user_id FROM users WHERE user_role = 'P' ORDER BY user_id LIMIT 1));

INSERT INTO board_comment (comment_content, comment_create_date, comment_is_use, board_id, user_id)
VALUES ('관리자 댓글입니다.', CURRENT_TIMESTAMP, 'Y',
        (SELECT MAX(board_id) FROM board WHERE board_is_use = 'Y'),
        (SELECT user_id FROM users WHERE user_role = 'A' ORDER BY user_id LIMIT 1));

-- 삭제(N)된 댓글 → 화면에 안 보여야 정상
INSERT INTO board_comment (comment_content, comment_create_date, comment_is_use, board_id, user_id)
VALUES ('삭제된 댓글 (보이면 안 됨)', CURRENT_TIMESTAMP, 'N',
        (SELECT MAX(board_id) FROM board WHERE board_is_use = 'Y'),
        (SELECT user_id FROM users WHERE user_role = 'S' ORDER BY user_id LIMIT 1));
