하이테크 LMS - Smart Learning Hub
스마트소프트웨어과 학생 · 교수 · 관리자가 함께 쓰는 학습관리시스템(LMS)
수업 · 과제 · 출석 · 공지를 역할별 대시보드 하나로 모았습니다.
개발 기간: 2026.09.22 ~ 2026.10.23
팀: 강경모(팀장) · 김경모 · 홍승훈
주요 기능
사용자	기능
공통	로그인 · 아이디(학번/사번) 찾기 · 비밀번호 찾기(휴대폰 인증) · 역할별 대시보드 · 마이페이지
학생	오늘의 수업 · 6자리 코드 출석 · 출석 현황 · 과제 제출(파일 첨부) · 점수/피드백 확인 · 시간표 · 수업 Q&A
교수	강의 · 차시 등록 · 수업 시작(출석 코드 발급) · 실시간 출석 현황 · 출석 달력 · 출석부 수정 · 과제 등록/채점 · 강의실
관리자	계정 등록 · 회원 관리(상태 변경) · 과목 관리 · 시스템 공지
커뮤니티	학교 공지 · 과목 공지 · 자유게시판 · 학과 안내(과정 소개 · 교육과정)
개발 환경
구분	내용
Language	Java 17
Framework	Spring Boot 4.1.1
Build	Gradle
IDE	Spring Tools 4.32.2 (STS)
ORM	Spring Data JPA
DB	H2 (개발용) / MariaDB (운영)
보안	Spring Security (폼 로그인, BCrypt)
View	Thymeleaf + Layout Dialect
기타	Lombok
패키지 구조
```text
com.hitech.lms
├── domain                    # 기능(도메인)별 Entity · Repository · Service · Controller
│   ├── user                  # 회원(학생/교수/관리자), 학과, 로그인     — users, dept
│   ├── course                # 과목, 개설강의, 수업 회차, 시간표      — subject, course, lesson, timetable
│   ├── study                 # 나의 학습: 과제, 제출, 출석            — homework, submit, att
│   ├── qna                   # 수업 Q&A                             — qna_board, answer
│   ├── board                 # 자유게시판, 공지사항                   — board, notice
│   ├── admin                 # 관리자 게시글, 회원 관리               — admin_post
│   └── file                  # 첨부파일                              — file_att, file_detail
│
└── global                    # 프로젝트 공통
    ├── config                # SecurityConfig 등 설정
    ├── error                 # 공통 예외 처리
    └── util                  # 공통 유틸리티
```
각 도메인 패키지 안에 `Entity`, `Repository`, `Service`, `Controller`를 함께 둡니다.
화면 파일은 `src/main/resources/templates/{도메인}/` 에 둡니다. (예: `templates/board/list.html`)
권한
코드 (`users.user_role`)	Security 권한	설명
S	ROLE_STUDENT	학생
P	ROLE_TEACHER	교수
A	ROLE_ADMIN	관리자 (계정 등록은 관리자만 가능)
브랜치 전략
```text
main                   # 발표 · 완성본
 └ dev                 # 개발 통합 브랜치
    └ feature/{기능}    # 개인 작업 (예: feature/board, feature/homework)
```
`dev`에서 `feature/{기능}` 브랜치를 만들어 작업
작업이 끝나면 `feature/{기능}` → `dev`로 Pull Request
발표 전에 `dev` → `main` 병합
⚠️ `main`, `dev`에 직접 push 하지 않기
⚠️ `bin/`, `build/`, `.metadata/`, DB 접속 정보 파일은 커밋하지 않기
로컬 실행
STS에서 File → Import → Gradle → Existing Gradle Project → `lms-backend` 폴더 선택
STS에 Lombok 설치 필요 (`java -jar lombok.jar`)
`LmsBackendApplication` 실행 → `http://localhost:8080`
H2 콘솔: `http://localhost:8080/h2-console`
JDBC URL은 `application.properties`의 `spring.datasource.url`과 동일하게 입력
---
📌 팀원 참고 사항
처음 세팅
STS → `File → Import → Gradle → Existing Gradle Project` → `lms-backend` 폴더
STS 워크스페이스는 Git 폴더와 다른 곳으로 (예: `C:\sts-workspace`)
STS에 Lombok 설치 (안 하면 getter/setter에 빨간 줄)
`Preferences → Gradle → Java home` = JDK 17
프로젝트 우클릭 → `Gradle → Refresh Gradle Project` (build.gradle 바뀔 때마다)
서버 실행 → `http://localhost:8080`
H2 콘솔 `http://localhost:8080/h2-console` (JDBC URL은 `application.properties`와 똑같이)
테스트 계정 INSERT (서버를 한 번 띄워 테이블이 생긴 뒤) → 아래 참고
테스트 데이터 쿼리
- 전에 공유했던 구글 드라이브에 'test-data.sql' 파일에 쿼리를 저장해 놓을테니, 확인후 직접 추가 바람.
내 기능용 데이터는 `docs/test-data-이름.sql` 처럼 사람별 파일로 따로 작성 (충돌 방지)
테스트 계정 (로컬 H2 전용, 비밀번호 `1234`)
로그인 주소: `/user/login`
아이디	구분	로그인 후 이동
2610001	1학년	/dashboard/student
2510001	2학년	/dashboard/student
2630001	하이테크	/dashboard/student
2090001	교수	/dashboard/teacher
0000001	관리자	/dashboard/admin
학번 규칙: 연도 2 + 과정 1 + 순번 4 (과정: 1 일반 / 3 하이테크 / 9 교수 / 0 관리자)
학년은 학번이 아니라 `users.user_grade` 컬럼으로 관리
권한: `users.user_role` → S 학생 / P 교수 / A 관리자
---
🛠 코드 작성 규칙 (SBB 방식)
수업에서 배운 SBB(점프 투 스프링부트) 방식으로 통일합니다.
Controller · Service 메서드 틀은 다 만들어 놨으니, 본문 주석 순서대로 채우면 됩니다.
항목	방식	참고 (SBB)
목록	`getList(page, kw)` → `Page<X>` 반환, `model.addAttribute("paging", paging)`	`QuestionService.getList`
상세	`getXxx(id)` → 없으면 `DataNotFoundException`	`QuestionService.getQuestion`
작성 · 수정	`@Valid XxxForm` + `BindingResult` → 오류 시 form 화면으로	`QuestionController.questionCreate`
로그인 사용자	`Principal principal` → `us.getUser(principal.getName())`	`QuestionController`
권한 체크	`@PreAuthorize("isAuthenticated()")` / `hasRole('TEACHER')` 등	
작성자 확인	작성자 ≠ 로그인 사용자 → `ResponseStatusException`	`questionModify`
Controller에 `@RequestMapping("/board")`가 있으면 메서드는 `@GetMapping("/list")`처럼 뒷부분만
Controller가 `return "board/list"` 하면 → `templates/board/list.html` (이름 똑같이)
새 화면은 `main.html` 복사 → `layout:fragment="content"` 안만 작성
⚠️ `layout.html`에는 `layout:decorate` 넣지 말 것 (무한 로딩)
JPA는 컬럼명을 밑줄로 바꿈 (`noticeIsUse` → `notice_is_use`) → INSERT는 h2-console의 실제 컬럼명 기준
---
🚧 충돌 방지 규칙
한 파일은 한 사람만 수정 → 담당 파일은 `docs/WORK_ASSIGNMENT.md` 참고
🔒 공통 파일은 강경모만 수정 → 필요하면 단톡으로 요청
`layout.html` · `navbar.html` · `SecurityConfig` · `build.gradle` · `application.properties` · `domain/user/*` · `MainController` · `DashboardController`
새 파일은 자기 패키지에 생성 (새 Service, 새 엔티티 등) → 충돌 안 남
기존 Entity 수정 시 단톡에 먼저 공유
---
🌿 Git
```bash
# 작업 시작
git switch dev
git fetch origin --prune                  # ① 지워진 브랜치 정리
git pull origin dev                       # ② 매일 아침 최신화
git switch -c feature/(기능명)             # ③ 기능 1개 = 브랜치 1개 (예: feature/board)

# 작업 끝
git add .
git commit -m "feat: 게시판 목록 구현"
git push origin feature/(기능명)           # ④ 작게 자주 push
# ⑤ GitHub에서 feature/(기능명) → dev 로 Pull Request → 팀장이 merge
```
`main`, `dev`에 직접 push 금지
`bin/`, `build/`, `.metadata/` 커밋 금지
GitHub 이슈에서 브랜치 만들었으면 → `git fetch origin` 후 `git switch feature/(기능명)`
커밋 메시지: `feat:` 기능 추가 / `fix:` 버그 수정 / `docs:` 문서 / `refactor:` 구조 변경
🏷 브랜치 이름 규칙
영어 소문자 · 숫자 · `-` 만 사용 → `feature/notice-create`, `feature/subject-2`, `feature/homework-submit`
❌ 한글 · 공백 · 특수문자(`[ ] · ( )` 등) 금지 → GitHub 경고, Git Extensions/터미널에서 브랜치가 안 지워지는 문제 생김
한글 설명은 PR 제목에만 → `[feature/notice-create] 공지 작성 부분`
GitHub 이슈에서 브랜치를 만들 때도 자동 생성된 이름(한글 포함)을 영어로 바꾼 뒤 만들기
체크아웃 확인 후 작업: 툴바/터미널에서 지금 브랜치가 `dev`가 아닌지 꼭 보기
🧹 PR merge 후 정리
merge된 브랜치는 PR 화면에서 Delete branch (작업 내용은 `dev`에 남아 있어서 안전, 필요하면 Restore branch)
각자 PC에서: `git fetch origin --prune` → `git switch dev` → `git pull origin dev`
Git Extensions: Pull ▼ Fetch all and prune → `dev` 체크아웃 → Pull ▼ Pull - merge → 툴바 `0 ↑ ↓` 확인
🆘 자주 나는 상황
상황	해결
Pull 할 때 `Your local changes ... would be overwritten`	커밋 안 한 수정이 있음 → 필요 없으면 리셋, 필요하면 Stash → Pull → Stash pop
실수로 `dev`에서 커밋함 (`dev` 옆 ↑)	push 금지! `git switch feature/(기능명)` → `git cherry-pick (커밋ID)` → push → `git switch dev` → `git reset --hard origin/dev`
PR에 `There are no new commits`	내 브랜치 커밋이 이미 `dev`에 다 들어가 있음 → 새 커밋을 push 했는지 확인, 없으면 PR 닫기
지워진 브랜치가 계속 보임	`git fetch origin --prune` (안 지워지면 `git update-ref -d "refs/remotes/origin/브랜치이름"`)
> ⚠️ `git reset --hard` 는 커밋 안 한 수정도 지움 → 실행 전 `git status` 로 확인