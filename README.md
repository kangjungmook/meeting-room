# 회의실 예약 시스템

사내 회의실 예약·관리·키오스크를 하나로 묶은 풀스택 시스템입니다. 실제 사내에서 운영하고 있습니다.

- 사용자: 일·주·월 캘린더로 전체 예약 현황을 보고 바로 예약합니다.
- 관리자: 별도 콘솔에서 회의실·회원·예약·알림·시스템을 통합 관리합니다.
- 키오스크: 로비에 설치한 화면에서 회의실별 현황을 확인하고 회의를 종료합니다.

포트폴리오: https://kangjungmook.vercel.app/projects/meeting-room

## 주요 기능

**사용자**
- 회원가입 후 관리자 승인을 받으면 로그인할 수 있습니다 (승인·거절·자동 승인 흐름)
- 회의실 예약 생성·수정, 내 예약 목록, 회의 조기 종료
- 푸시 알림 수신 설정과 리마인더 시점 설정

**관리자**
- 대시보드, 회의실 등록·수정·삭제·사용 중지, 예약 관리
- 회원 승인·거절, 권한 변경, 비밀번호 초기화, 계정 직접 생성
- 관리 로그: 로그인·예약·회원 관리 등 모든 액션을 IP·디바이스·브라우저와 함께 기록
- 전체 알림 발송, 알림 설정
- 점검 모드: 켜면 관리자를 제외한 API 요청을 503으로 차단
- 예약·로그·회원 데이터 초기화

**공통**
- 화면 4종: 사용자 / 관리자 / 키오스크 / 모바일
- SSE로 예약 변경을 실시간 반영
- Firebase FCM 푸시 알림: 예약 생성·변경·취소 알림, 시작 전 리마인더 (1분 주기 스케줄러)
- 1개월이 지난 관리 로그는 매일 03:00에 자동 삭제

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5, Spring Security, JWT (jjwt), Spring Data JPA |
| Database | MariaDB |
| 알림 | Firebase Admin SDK (FCM), Server-Sent Events |
| Frontend | Vue 3, Vue Router, Vite, Tailwind CSS, Axios, Day.js |

## 폴더 구조

```
.
├── meeting-room/        # 백엔드 (Spring Boot, Gradle)
│   └── src/main/java/com/example/meetingroom/
│       ├── controller/  # REST API
│       ├── service/     # 비즈니스 로직 (예약, 인증, FCM, SSE, 관리)
│       ├── repository/  # JPA 레포지토리
│       ├── domain/      # 엔티티
│       ├── dto/
│       ├── scheduler/   # 리마인더 알림, 로그 정리
│       └── common/config/  # 보안, JWT 필터, 점검 모드 필터, Firebase 설정
└── frontend/            # 프론트엔드 (Vue 3, Vite)
    └── src/
        ├── pages/       # app / admin / kiosk / auth / system
        ├── composables/ # 화면별 상태·로직
        └── router/
```

## 인증과 권한

- 로그인하면 액세스 토큰과 리프레시 토큰을 발급하고, 만료되면 프론트에서 자동으로 재발급합니다.
- `/api/auth/**`, 회의실·예약 조회, 키오스크 종료는 로그인 없이 접근할 수 있고 `/api/admin/**`는 `ADMIN` 권한이 필요합니다. 그 외 `/api/**`는 로그인이 필요합니다.
- 최초 실행 시 `DataInitializer`가 기본 관리자 계정을 만듭니다. 운영 환경에서는 로그인 후 바로 비밀번호를 변경하세요.

## 실행 방법

### 준비물

- JDK 21
- MariaDB (데이터베이스 이름 `room_db`)
- Node.js 20.19 이상 (Vite 7 요구사항)

### 백엔드

1. `meeting-room/src/main/resources/application-example.yml`을 같은 폴더에 `application.yml`로 복사합니다. (`application.yml`은 `.gitignore`에 포함되어 있습니다)
2. 파일을 열어 `app.jwt.secret`(32바이트 이상)과 DB 접속 정보를 본인 환경에 맞게 바꿉니다. 기본 프로필은 `dev`이고 로컬 MariaDB를 바라봅니다.
3. 푸시 알림을 쓰려면 Firebase 서비스 계정 키를 `meeting-room/src/main/resources/firebase-service-account.json`에 둡니다. 파일이 없으면 경고 로그만 남기고 알림 기능만 꺼진 채 실행됩니다.
4. 실행합니다.

```bash
cd meeting-room
./gradlew bootRun     # http://localhost:8080
```

### 프론트엔드

```bash
cd frontend
npm install
npm run dev           # http://localhost:5173  (/api 요청은 localhost:8080으로 프록시)
```

프론트엔드의 `src/firebase.js`와 `public/firebase-messaging-sw.js`에는 Firebase 웹 앱 설정이 들어 있습니다. 본인 Firebase 프로젝트를 쓰려면 두 파일의 설정값과 VAPID 키를 바꿔야 합니다.

### 운영 환경

`prod` 프로필(`application-example.yml` 참고)은 PKCS12 키스토어로 HTTPS를 켜고, 80번 포트 요청을 30001번 포트로 리다이렉트합니다. DB 계정과 키스토어 정보는 운영 서버의 `application.yml`에만 두고 저장소에는 올리지 않습니다.

백엔드는 `classpath:/static/`의 정적 파일을 함께 서빙하고, 없는 경로는 `index.html`로 보냅니다. 그래서 프론트엔드 빌드 결과(`frontend/dist`)를 `meeting-room/src/main/resources/static/`에 넣으면 한 개의 서버로 화면까지 제공할 수 있습니다.

## API 개요

| 경로 | 설명 |
| --- | --- |
| `/api/auth` | 회원가입, 로그인, 토큰 재발급, 로그아웃, 비밀번호 변경 |
| `/api/rooms` | 회의실 조회 |
| `/api/bookings` | 예약 조회·생성·수정, 내 예약, 종료(키오스크 종료 포함) |
| `/api/users` | 내 정보, FCM 토큰 등록·해제, 알림 설정 |
| `/api/sse/subscribe` | 실시간 이벤트 구독 |
| `/api/admin` | 대시보드, 회의실·예약·회원·로그·알림·초기화 관리 |
