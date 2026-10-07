# 하이테크 LMS - Backend

Spring Boot 4.1.1 · Java 17 · Gradle · JPA · H2 · Spring Security · Thymeleaf

---

## 📌 팀원 참고 사항

### 처음 세팅
1. STS → `File → Import → Gradle → Existing Gradle Project` → `lms-backend` 폴더
   - STS **워크스페이스는 Git 폴더와 다른 곳**으로 (예: `C:\sts-workspace`)
2. STS에 **Lombok 설치** (안 하면 getter/setter에 빨간 줄)
3. `Preferences → Gradle → Java home` = **JDK 17**
4. 서버 실행 → `http://localhost:8080`
5. H2 콘솔 `http://localhost:8080/h2-console` (JDBC URL은 `application.properties`와 똑같이)
6. 테스트 계정은 `docs/test-data.sql` 실행 (서버를 한 번 띄워 테이블이 생긴 뒤)

### 테스트 계정 (로컬 H2 전용, 비밀번호 `1234`)
| 아이디 | 구분 | 로그인 후 이동 |
|---|---|---|
| 2610001 | 1학년 | /dashboard/student |
| 2510001 | 2학년 | /dashboard/student |
| 2630001 | 하이테크 | /dashboard/student |
| 2090001 | 교수 | /dashboard/teacher |
| 2600001 | 관리자 | /dashboard/admin |

- 학번 규칙: **연도 2 + 과정 1 + 순번 4** (과정: 1 일반 / 3 하이테크 / 9 교수 / 0 관리자)
- 학년은 학번이 아니라 `users.user_grade` 컬럼으로 관리
- 권한: `users.user_role` → S 학생 / P 교수 / A 관리자

### 작업 규칙
- 내 담당 **Controller · Service의 TODO 주석**부터 채우기
- **공통 파일은 수정 전에 팀에 공유**: Entity, `SecurityConfig`, `layout.html`
- 새 화면은 `main.html`을 복사해서 `layout:fragment="content"` 안만 작성
- ⚠️ `layout:decorate="~{layout}"`는 **개별 화면에만**. `layout.html`에 넣으면 무한 반복으로 페이지가 멈춤
- Controller 클래스에 `@RequestMapping("/board")`가 있으면 메서드는 `@GetMapping("/list")`처럼 **뒷부분만**
- JPA는 컬럼명을 밑줄로 바꿈 (`noticeIsUse` → `notice_is_use`) → INSERT는 h2-console의 실제 컬럼명 기준
- 로그아웃은 **POST만** 가능 (`<form th:action="@{/logout}" method="post">`)

### Git
```bash
git fetch origin
git switch -c feature/(기능명) origin/dev   # dev에서 내 브랜치
git add . → git commit -m "내용" → git push origin feature/(기능명)
# GitHub에서 feature/(기능명) → dev 로 Pull Request
```
- `main`, `dev`에 직접 push 금지
- `bin/`, `build/`, `.metadata/` 커밋 금지
- 작업 시작 전 `pull`, 끝나면 `push`

---

## 📁 파일 구조
```text
src/main/java/com/hitech/lms
├── LmsBackendApplication.java
├── domain
│   ├── user        Users, Dept / UsersRepository, DeptRepository
│   │               UserService(계정 등록), UserSecurityService(로그인 조회·권한)
│   │               LoginController            GET /login                         [강경모]
│   ├── main        MainController             GET /                              [강경모]
│   │               DashboardController        /dashboard → 권한별 이동            [강경모]
│   ├── course      Subject · Course · Lesson · Timetable  (Entity·Repo·Service·Controller)  [김경모]
│   │               ReplayController           /replay                            [홍승훈]
│   ├── study       Att (출석)  /attendance                                        [김경모]
│   │               Homework · Submit (과제·제출)  /homework                        [홍승훈]
│   ├── qna         QnaBoard · Answer  /qna                                        [홍승훈]
│   ├── board       Board (자유게시판)  /board                                      [홍승훈]
│   │               Notice (공지)  /notice                                         [김경모]
│   ├── admin       AdminPost  /admin  (계정 등록은 UserService.create())           [김경모]
│   └── file        FileAtt · FileDetail / FileService                            [공통]
└── global
    └── config      SecurityConfig  (로그인 · 권한 · PasswordEncoder)

src/main/resources
├── application.properties
└── templates
    ├── layout.html               공통 틀 (header · 메뉴 · footer)
    ├── main.html                 메인 + 새 화면 만들 때 복사용 샘플
    ├── login.html                로그인
    ├── dashboard/                student.html · teacher.html · admin.html
    └── admin/ attendance/ board/ course/ homework/ lesson/
        notice/ qna/ replay/ subject/ timetable/     도메인별 화면 폴더

docs
└── test-data.sql                 테스트 계정 INSERT
```