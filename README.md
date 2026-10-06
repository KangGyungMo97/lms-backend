# 하이테크 LMS - Backend

학생 · 교수 · 관리자가 사용하는 학습관리시스템(LMS) 백엔드

## 개발 환경
| 구분 | 내용 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.1 |
| Build | Gradle |
| IDE | Spring Tools 4.32.2 (STS) |
| ORM | Spring Data JPA |
| DB | H2 (개발용) / MariaDB (운영) |
| 보안 | Spring Security (폼 로그인, BCrypt) |
| View | Thymeleaf + Layout Dialect |
| 기타 | Lombok |

## 패키지 구조
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
- 각 도메인 패키지 안에 `Entity`, `Repository`, `Service`, `Controller`를 함께 둡니다.
- 화면 파일은 `src/main/resources/templates/{도메인}/` 에 둡니다. (예: `templates/board/list.html`)

## 권한
| 코드 (`users.user_role`) | Security 권한 | 설명 |
|---|---|---|
| S | ROLE_STUDENT | 학생 |
| P | ROLE_TEACHER | 교수 |
| A | ROLE_ADMIN | 관리자 (계정 등록은 관리자만 가능) |

## 브랜치 전략
```text
main                   # 발표 · 완성본
 └ dev                 # 개발 통합 브랜치
    └ feature/{기능}    # 개인 작업 (예: feature/board, feature/homework)
```
1. `dev`에서 `feature/{기능}` 브랜치를 만들어 작업
2. 작업이 끝나면 `feature/{기능}` → `dev`로 Pull Request
3. 발표 전에 `dev` → `main` 병합
- ⚠️ `main`, `dev`에 직접 push 하지 않기
- ⚠️ `bin/`, `build/`, `.metadata/`, DB 접속 정보 파일은 커밋하지 않기

## 로컬 실행
1. STS에서 **File → Import → Gradle → Existing Gradle Project** → `lms-backend` 폴더 선택
2. STS에 **Lombok** 설치 필요 (`java -jar lombok.jar`)
3. `LmsBackendApplication` 실행 → `http://localhost:8080`
4. H2 콘솔: `http://localhost:8080/h2-console`
   - JDBC URL은 `application.properties`의 `spring.datasource.url`과 동일하게 입력