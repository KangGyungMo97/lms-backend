-- 정보마당 시스템 공지 테스트 데이터 (홍승훈)
-- 먼저 구글 드라이브 test-data.sql 을 실행해서 관리자 계정(0000001)이 있어야 함
-- 공지는 관리자가 쓰는 글이라 작성자를 관리자(0000001)로 넣음

INSERT INTO admin_post (admin_post_title, admin_post_content, admin_post_create_date, admin_post_update_date, admin_post_view, admin_post_is_use, user_id) VALUES
  ('[필독] 하이테크 LMS 이용 안내', '하이테크 LMS 에 오신 것을 환영합니다.
로그인 아이디는 학번(교수는 사번)이며, 첫 로그인 후 비밀번호를 변경해 주세요.', CURRENT_DATE, CURRENT_DATE, 25, 'Y', '0000001'),
  ('10월 정기 점검 안내', '10월 18일(토) 02:00 ~ 06:00 서버 점검으로 LMS 이용이 중단됩니다.', CURRENT_DATE, CURRENT_DATE, 12, 'Y', '0000001'),
  ('자유게시판 이용 규칙 안내', '욕설, 광고, 개인정보가 포함된 글은 관리자가 삭제할 수 있습니다.', CURRENT_DATE, CURRENT_DATE, 7, 'Y', '0000001'),
  ('삭제된 공지 (보이면 안 됨)', '이 공지는 목록에 나오면 안 됩니다.', CURRENT_DATE, CURRENT_DATE, 0, 'N', '0000001'),
  ('페이징 테스트 공지 1', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001'),
  ('페이징 테스트 공지 2', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001'),
  ('페이징 테스트 공지 3', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001'),
  ('페이징 테스트 공지 4', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001'),
  ('페이징 테스트 공지 5', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001'),
  ('페이징 테스트 공지 6', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001'),
  ('페이징 테스트 공지 7', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001'),
  ('페이징 테스트 공지 8', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001'),
  ('페이징 테스트 공지 9', '페이징 확인용 공지입니다.', CURRENT_DATE, CURRENT_DATE, 0, 'Y', '0000001');

-- 확인 (사용 중 12건 + 삭제 1건 = 13건)
SELECT admin_post_id, admin_post_title, admin_post_is_use FROM admin_post;
