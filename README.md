# 하이테크 LMS - Backend

Spring Boot 4.1.1 · Java 17 · Gradle · JPA · H2 · Spring Security · Thymeleaf

> 📋 **각자 담당 기능 · 파일 · 우선순위 → [`docs/WORK_ASSIGNMENT.md`](docs/WORK_ASSIGNMENT.md)**
> 📅 마감 **10/23** · P1 **~10/14** · P2 **~10/19** · 10/20~22 통합 테스트

## 📊 진행 현황 (10/13 · dev PR #26 기준)

- P1 마감: **10/14 (내일)** / P2 마감: 10/19 / 10/20~22 통합 테스트
- 로그인: `/user/login` (테스트 비밀번호 `1234`)

### 1. 한눈에 보기

| 구분 | 완료 / 전체 | 진행률 |
|---|:---:|:---:|
| **P1 전체** | **5 / 10** | 50% |
| P2 전체 | 3 / 5 | 60% |

| 팀원 | P1 | P2 | 한 줄 요약 |
|:---:|:---:|:---:|---|
| 강경모 | 3 / 5 | - | 과제 완료, Q&A 확인 중 |
| 김경모 | **1 / 4** | 1 / 3 | 공지 완료(PR #26), 과목 PR 완료·화면 0개, 수업 미착수 |
| 홍승훈 | **1 / 1** | **2 / 2** | P1 · P2 모두 완료 |

### 2. 남은 기능

> 취소선 = 구현 완료

#### 강경모

**공통**
- ~~로그인(SBB, `/user/login`) · 로그아웃 · 권한별 대시보드~~
- ~~레이아웃 · SecurityConfig(`@EnableMethodSecurity`) · DataNotFoundException~~

**P1**
- ~~과제 목록 · 상세 · 등록~~
- ~~과제 제출 (마감 · 중복 체크)~~
- ~~제출현황 · 채점 (재채점 가능)~~
- Q&A 질문 + 답변 등록 → 코드는 반영됨, **동작 확인 필요**
- 과제 · 제출 · Q&A 수정 · 삭제
- 첨부파일 업로드 · 다운로드 (`FileService`, 공통)
- 출결 관련 아이디어 및 구현하기
- HTML 수정

**P3 (안 해도 됨)**
- 제출물 zip 다운로드
- 다시보기 · 이어보기

#### 김경모

**P1**
- ~~공지 목록 · 상세 · 작성 (화면 포함, PR #26)~~
- 과목 목록 · 상세 → ~~PR 올리기~~ (PR #21 · #24 merge 완료), **화면(html) 필요** (`templates/subject/` 비어 있음)
- **수업 목록 · 상세** → 미착수 (Q&A · 과제가 붙어 있어서 **가장 급함**)
- 계정 등록 · 회원 목록 → 미착수

**P2**
- ~~공지 수정 · 삭제 (`notice/form.html` 재사용)~~
- 과목 등록 · 수정 · 삭제 → Java 완료, **화면 필요**
- 시간표 → 미착수

**P3 (안 해도 됨)**
- 회원 상태변경
- 관리자 게시글 목록
- 학과관리

#### 홍승훈

**P1**
- ~~자유게시판 목록 · 상세 · 작성 · 수정 · 삭제 (검색, 페이징, 조회수)~~

**P2**
- ~~정보마당 시스템 공지 목록 · 상세 (이전 · 다음 글)~~
- ~~기관소개~~

**P3 (안 해도 됨)**
- 게시판 · 공지 첨부 다운로드 (FileService 완성 후)
- 댓글 (ERD에 테이블 없음)

#### 공통: 화면 디자인 (HTML 꾸미기)

지금 화면들은 기능 확인용으로 각 html 안에 `<style>`을 따로 넣어 둔 상태라, 디자인이 통일돼 있지 않음.
**P1 기능을 먼저 끝내고, 10/15 ~ 10/19 (P2 기간)에 진행.**

- 강경모: 공통 디자인 틀 만들기 (피그마 시안 기준)
  - `static/css/common.css` 새로 만들기 (색상, 버튼, 표, 폼, 페이징, 배지 등 공통 스타일)
  - `layout.html` 헤더 · 메뉴 · 푸터 디자인
  - 메인 · 로그인 · 대시보드 화면
- 각자: 자기 화면에 공통 스타일 적용
  - 각 html의 `<style>` 블록을 지우고, `common.css`의 공통 클래스(`btn`, `board`, `paging` 등)로 바꾸기
  - 강경모: homework, qna
  - 김경모: notice, subject, lesson, admin, timetable
  - 홍승훈: board, info, about

#### 팀 협의 필요
- 수강신청(학생-과목) 테이블이 없음 → 지금은 학생이 **전체 과제 · 공지**를 봄
- 출결 상태 코드(Entity 기준 출석 Y / 미출석 N / 지각 L / 공결 E) · 출석률 계산식 (출석은 보류 상태)
- 수정 URL `/modify` vs `/update`, 삭제 GET vs POST (URL 설계표 그대로 진행 중)

### 3. 파일 지도

> <del>취소선</del> = 완료 · <b>굵게</b> = 진행중 · 그냥 글자 = 미착수 · (보류) = 안 해도 됨
> 화면은 기능 기준 (디자인은 전부 미착수)

<pre>
src/main/java/com/hitech/lms
├── domain
│   ├── user     <del>Users · Dept · UserService · UserSecurityService · UserController</del>              (강경모)
│   ├── main     <del>MainController · DashboardController</del>                                           (강경모)
│   │            <del>InfoController · InfoService · InfoNoticeRepository</del>                            (홍승훈)
│   ├── board    <del>BoardController · BoardService · BoardRepository</del>                               (홍승훈)
│   │            <del>NoticeController · NoticeService</del>                                               (김경모)
│   ├── course   <b>SubjectController · SubjectService</b> → html 필요                                 (김경모)
│   │            LessonController · LessonService                                               (김경모)
│   │            TimetableController · TimetableService                                         (김경모)
│   │            ReplayController (보류)                                                        (강경모)
│   ├── study    <del>HomeworkController · HomeworkService</del> → 수정 · 삭제만 남음                      (강경모)
│   │            AttController · AttService (보류)
│   ├── qna      <b>QnaController · QnaService</b> → 동작 확인 필요                                    (강경모)
│   ├── admin    AdminController · AdminService                                                 (김경모)
│   └── file     FileService                                                                    (강경모)
└── global
    ├── config   <del>SecurityConfig</del>                                                                 (강경모)
    └── error    <del>DataNotFoundException</del>                                                          (강경모)

src/main/resources/templates
├── <del>layout.html · main.html · login.html · dashboard/</del>                                           (강경모)
├── <del>homework/  list · detail · form · submit_list · submit_detail</del>                               (강경모)
├── <b>qna/       list · detail · form</b> → 동작 확인 필요                                            (강경모)
├── <del>board/     list · detail · form</del>                                                             (홍승훈)
├── <del>info/      notice_list · notice_detail</del>                                                      (홍승훈)
├── <del>about/     course</del>                                                                           (홍승훈)
├── <del>notice/    list · detail · form</del>                                                             (김경모)
├── subject/   list · detail · form                                                             (김경모)
├── lesson/    list · detail                                                                    (김경모)
├── admin/     user_form · user_list                                                            (김경모)
├── timetable/ list                                                                             (김경모)
└── replay/ (보류)                                                                              (강경모)

src/main/resources/static
└── css/common.css → 공통 디자인 (새로 만들 파일)                                               (강경모)

docs
├── test-data-강경모-과제.sql
├── test-data-홍승훈.sql
└── test-data-홍승훈-공지.sql
</pre>

---

## 📌 팀원 참고 사항

### 처음 세팅
1. STS → `File → Import → Gradle → Existing Gradle Project` → `lms-backend` 폴더
   - STS **워크스페이스는 Git 폴더와 다른 곳**으로 (예: `C:\sts-workspace`)
2. STS에 **Lombok 설치** (안 하면 getter/setter에 빨간 줄)
3. `Preferences → Gradle → Java home` = **JDK 17**
4. 프로젝트 우클릭 → `Gradle → Refresh Gradle Project` (**build.gradle 바뀔 때마다**)
5. 서버 실행 → `http://localhost:8080`
6. H2 콘솔 `http://localhost:8080/h2-console` (JDBC URL은 `application.properties`와 똑같이)
7. 테스트 계정 INSERT (서버를 한 번 띄워 테이블이 생긴 뒤) → 아래 참고

### 테스트 데이터 쿼리
**- 전에 공유했던 구글 드라이브에 'test-data.sql' 파일에 쿼리를 저장해 놓을테니, 확인후 직접 추가 바람.**
- 내 기능용 데이터는 `docs/test-data-이름.sql` 처럼 **사람별 파일**로 따로 작성 (충돌 방지)

### 테스트 계정 (로컬 H2 전용, 비밀번호 `1234`)
로그인 주소: **`/user/login`**

| 아이디 | 구분 | 로그인 후 이동 |
|---|---|---|
| 2610001 | 1학년 | /dashboard/student |
| 2510001 | 2학년 | /dashboard/student |
| 2630001 | 하이테크 | /dashboard/student |
| 2090001 | 교수 | /dashboard/teacher |
| 0000001 | 관리자 | /dashboard/admin |

- 학번 규칙: **연도 2 + 과정 1 + 순번 4** (과정: 1 일반 / 3 하이테크 / 9 교수 / 0 관리자)
- 학년은 학번이 아니라 `users.user_grade` 컬럼으로 관리
- 권한: `users.user_role` → S 학생 / P 교수 / A 관리자

---

## 🛠 코드 작성 규칙 (SBB 방식)

수업에서 배운 **SBB(점프 투 스프링부트)** 방식으로 통일합니다.
Controller · Service 메서드 틀은 다 만들어 놨으니, **본문 주석 순서대로 채우면 됩니다.**

| 항목 | 방식 | 참고 (SBB) |
|---|---|---|
| 목록 | `getList(page, kw)` → `Page<X>` 반환, `model.addAttribute("paging", paging)` | `QuestionService.getList` |
| 상세 | `getXxx(id)` → 없으면 `DataNotFoundException` | `QuestionService.getQuestion` |
| 작성 · 수정 | `@Valid XxxForm` + `BindingResult` → 오류 시 form 화면으로 | `QuestionController.questionCreate` |
| 로그인 사용자 | `Principal principal` → `us.getUser(principal.getName())` | `QuestionController` |
| 권한 체크 | `@PreAuthorize("isAuthenticated()")` / `hasRole('TEACHER')` 등 | |
| 작성자 확인 | 작성자 ≠ 로그인 사용자 → `ResponseStatusException` | `questionModify` |

- Controller에 `@RequestMapping("/board")`가 있으면 메서드는 `@GetMapping("/list")`처럼 **뒷부분만**
- Controller가 `return "board/list"` 하면 → `templates/board/list.html` (**이름 똑같이**)
- 새 화면은 `main.html` 복사 → `layout:fragment="content"` 안만 작성
- ⚠️ **`layout.html`에는 `layout:decorate` 넣지 말 것** (무한 로딩)
- JPA는 컬럼명을 밑줄로 바꿈 (`noticeIsUse` → `notice_is_use`) → INSERT는 h2-console의 실제 컬럼명 기준

---

## 🚧 충돌 방지 규칙

1. **한 파일은 한 사람만 수정** → 담당 파일은 `docs/WORK_ASSIGNMENT.md` 참고
2. **🔒 공통 파일은 강경모만 수정** → 필요하면 단톡으로 요청
   - `layout.html` · `navbar.html` · `SecurityConfig` · `build.gradle` · `application.properties` · `domain/user/*` · `MainController` · `DashboardController`
3. **새 파일은 자기 패키지에 생성** (새 Service, 새 엔티티 등) → 충돌 안 남
4. **기존 Entity 수정 시** 단톡에 먼저 공유

---

## 🌿 Git

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

- `main`, `dev`에 직접 push 금지
- `bin/`, `build/`, `.metadata/` 커밋 금지
- GitHub 이슈에서 브랜치 만들었으면 → `git fetch origin` 후 `git switch feature/(기능명)`
- 커밋 메시지: `feat:` 기능 추가 / `fix:` 버그 수정 / `docs:` 문서 / `refactor:` 구조 변경

### 🏷 브랜치 이름 규칙
- **영어 소문자 · 숫자 · `-` 만** 사용 → `feature/notice-create`, `feature/subject-2`, `feature/homework-submit`
- ❌ 한글 · 공백 · 특수문자(`[ ] · ( )` 등) 금지 → GitHub 경고, Git Extensions/터미널에서 브랜치가 안 지워지는 문제 생김
- 한글 설명은 **PR 제목**에만 → `[feature/notice-create] 공지 작성 부분`
- GitHub 이슈에서 브랜치를 만들 때도 자동 생성된 이름(한글 포함)을 **영어로 바꾼 뒤** 만들기
- 체크아웃 확인 후 작업: 툴바/터미널에서 지금 브랜치가 `dev`가 **아닌지** 꼭 보기

### 🧹 PR merge 후 정리
- merge된 브랜치는 PR 화면에서 **Delete branch** (작업 내용은 `dev`에 남아 있어서 안전, 필요하면 Restore branch)
- 각자 PC에서: `git fetch origin --prune` → `git switch dev` → `git pull origin dev`
- Git Extensions: Pull ▼ **Fetch all and prune** → `dev` 체크아웃 → Pull ▼ **Pull - merge** → 툴바 `0 ↑ ↓` 확인

### 🆘 자주 나는 상황
| 상황 | 해결 |
|---|---|
| Pull 할 때 `Your local changes ... would be overwritten` | 커밋 안 한 수정이 있음 → 필요 없으면 리셋, 필요하면 **Stash → Pull → Stash pop** |
| 실수로 `dev`에서 커밋함 (`dev` 옆 ↑) | **push 금지!** `git switch feature/(기능명)` → `git cherry-pick (커밋ID)` → push → `git switch dev` → `git reset --hard origin/dev` |
| PR에 `There are no new commits` | 내 브랜치 커밋이 이미 `dev`에 다 들어가 있음 → 새 커밋을 push 했는지 확인, 없으면 PR 닫기 |
| 지워진 브랜치가 계속 보임 | `git fetch origin --prune` (안 지워지면 `git update-ref -d "refs/remotes/origin/브랜치이름"`) |

> ⚠️ `git reset --hard` 는 커밋 안 한 수정도 지움 → 실행 전 `git status` 로 확인

---

## 📁 파일 구조

```text
src/main/java/com/hitech/lms
├── LmsBackendApplication.java
├── domain
│   ├── user     Users · Dept / UsersRepository · DeptRepository
│   │            UserRole (ADMIN · TEACHER · STUDENT)
│   │            UserService (계정 등록 · getUser) · UserSecurityService (로그인 조회 · 권한)
│   │            UserController           /user/login · /user/logout
│   ├── main     MainController           /  · /about/course
│   │            DashboardController      /dashboard → 권한별 이동
│   │            InfoController           /info/notice  (시스템 공지 조회)
│   ├── board    Board · Notice           /board · /notice
│   │            BoardForm · CommentForm · NoticeForm
│   ├── course   Subject · Course · Lesson · Timetable
│   │            SubjectController /subject · LessonController /lesson
│   │            TimetableController /timetable · ReplayController /replay
│   │            SubjectForm
│   ├── study    Homework · Submit · Att  /homework · /attendance
│   │            HomeworkForm · SubmitForm · GradeForm
│   ├── qna      QnaBoard · Answer        /qna
│   │            QnaForm · AnswerForm
│   ├── admin    AdminPost                /admin  (계정 등록 → UserService.create())
│   │            UserCreateForm
│   └── file     FileAtt · FileDetail / FileService (첨부파일 공통)
└── global
    ├── config   SecurityConfig  (로그인 · 로그아웃 · PasswordEncoder · @PreAuthorize)
    └── error    DataNotFoundException

src/main/resources
├── application.properties
├── static                         bootstrap.css · bootstrap.js · style.css
└── templates
    ├── layout.html                공통 틀 (navbar + content)
    ├── navbar.html                메뉴 (로그인 / 로그아웃 자동 전환)
    ├── form_errors.html           입력 오류 메시지 (SBB 동일)
    ├── main.html                  메인 + 새 화면 만들 때 복사용 샘플
    ├── login_form.html            로그인
    ├── dashboard_student.html · dashboard_teacher.html · dashboard_admin.html
    └── admin/ about/ board/ homework/ info/ lesson/
        notice/ qna/ replay/ subject/ timetable/     도메인별 화면 폴더

docs
├── WORK_ASSIGNMENT.md             담당 · 파일 · 우선순위
└── test-data-이름.sql              각자 테스트 데이터
```
