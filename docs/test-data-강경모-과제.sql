-- 과제 화면 테스트용 (H2 콘솔에서 실행, 테스트 계정 SQL 을 먼저 넣은 뒤)
-- 교수 2090001 이 담당하는 과목 1개 · 수업 2회차 · 과제 2개
INSERT INTO subject (subject_id, subject_name, dept_id)
VALUES ('DB101', '데이터베이스', (SELECT dept_id FROM dept LIMIT 1));

INSERT INTO course (subject_id, course_title, course_content, course_is_use, course_weekday, course_start_time, course_end_time, user_id)
VALUES ('DB101', '데이터베이스 1분반', 'SQL 기초부터 설계까지', 'Y', '1', 1, 3, '2090001');

INSERT INTO lesson (lesson_title, lesson_content, course_id)
VALUES ('5주차 정규화', '1~3정규형', (SELECT MAX(course_id) FROM course)),
       ('6주차 조인', 'INNER / OUTER JOIN', (SELECT MAX(course_id) FROM course));

INSERT INTO homework (homework_title, homework_content, homework_start_date, homework_end_date, lesson_id)
VALUES ('정규화 실습', '주어진 테이블을 3NF까지 정규화하세요.', DATEADD('DAY', -10, CURRENT_DATE), DATEADD('DAY', -3, CURRENT_DATE), (SELECT MIN(lesson_id) FROM lesson)),
       ('조인 쿼리 작성', '예제 테이블로 조인 쿼리 5개를 작성하세요.', CURRENT_DATE, DATEADD('DAY', 5, CURRENT_DATE), (SELECT MAX(lesson_id) FROM lesson));
