# dependencies
* SpringBoot - 4.1.1  
* Spring Tools - 4.32.2  
* jpa  
* mariaDB  
* h2  



# 패키지 구조
```text
com.hitech.lms (기본 패키지)
├── domain<br>
│   ├── user          # 회원 (학생, 교수)  
│   ├── admin         # 관리자 전용 (회원/강의 관리 및 승인)  
│   ├── course        # 강의 및 수강신청  
│   ├── assignment    # 과제 및 제출  
│   └── board         # 게시판 / 공지사항  
│  
└── global            # 프로젝트 전체에서 공통으로 쓰는 클래스들  
    ├── config        # Security, WebConfig(CORS) 등 설정  
    ├── error         # 공통 에러 처리 및 예외 클래스  
    └── util          # S3 파일 업로드, 공통 유틸리티  
```
