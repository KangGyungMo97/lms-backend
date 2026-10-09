# 하이테크 LMS - Backend

Spring Boot 4.1.1 · Java 17 · Gradle · JPA · H2 · Spring Security · Thymeleaf

> 📋 **각자 담당 기능 · 파일 · 우선순위 → [`docs/WORK_ASSIGNMENT.md`](docs/WORK_ASSIGNMENT.md)**
> 📅 마감 **10/23** · P1 **~10/14** · P2 **~10/19** · 10/20~22 통합 테스트

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
git pull origin dev                       # ① 매일 아침 최신화
git switch -c feature/(기능명)             # ② 기능 1개 = 브랜치 1개 (예: feature/board)

# 작업 끝
git add .
git commit -m "feat: 게시판 목록 구현"
git push origin feature/(기능명)           # ③ 작게 자주 push
# ④ GitHub에서 feature/(기능명) → dev 로 Pull Request → 팀장이 merge
```

- `main`, `dev`에 직접 push 금지
- `bin/`, `build/`, `.metadata/` 커밋 금지
- GitHub 이슈에서 브랜치 만들었으면 → `git fetch origin` 후 `git switch feature/(기능명)`
- 커밋 메시지: `feat:` 기능 추가 / `fix:` 버그 수정 / `docs:` 문서 / `refactor:` 구조 변경

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
