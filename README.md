# 🚚 404-found-delivery — 배달 음식 주문 관리 플랫폼

 손님, 가게 사장님, 관리자가 각자의 권한으로 하나의 배달 서비스를 함께 운영하는 백엔드 API 서버를 만들었습니다.

---

## 📖 개요

광화문 지역 음식점을 대상으로, 주문·결제·리뷰 흐름 전체를 백엔드로 구현했습니다. 사용자는 회원가입 후 역할에 따라 CUSTOMER, OWNER, MANAGER, MASTER 네 가지로 나뉘며, 각 역할마다 접근 가능한 API와 데이터 범위가 다릅니다.

- 🙋 **손님**: 가게 탐색, 장바구니, 주문, 결제, 리뷰 작성
- 🏪 **사장님**: 가게/메뉴 등록 및 관리, 주문 접수 처리
- 🛡️ **관리자** (MANAGER, MASTER)
  - **매니저(MANAGER)** 는 가게 승인, 지역/카테고리 관리, 전체 데이터 조회 권한을 가지며
  - **마스터(MASTER)** 는 여기에 더해 매니저 계정을 생성·조회·수정·삭제할 수 있는 최종 관리자입니다.

---

## 👥 팀 구성

| 이름 | 담당 도메인 |
|---|---|
| 🔧 민지 | User, 공통(Global) — 인증/보안, 예외처리, 응답 포맷 |
| 🏬 서인 | Store, Category, Region |
| 🍜 초인 | Menu, AiRequest, Cart, CartItem |
| 📦 우현 | Order, OrderItem, Address |
| 💳 제희 | Payment, Review |

---

## 🛠 기술 스택

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](#)
[![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](#)
[![Gemini](https://img.shields.io/badge/Gemini%20AI-4285F4?style=for-the-badge&logo=googlegemini&logoColor=white)](#)
[![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)](#)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](#)

| 영역 | 사용 기술 |
|---|---|
| 언어/프레임워크 | Java 17, Spring Boot 3 |
| 인증 | Spring Security, JWT (Stateless) |
| 데이터베이스 | PostgreSQL, Spring Data JPA (Hibernate 6) |
| 빌드 | Gradle |
| 문서화 | Swagger (springdoc-openapi) |
| 외부 연동 | Gemini API(메뉴 설명 생성), AWS S3(이미지 저장) |
| 기타 | Lombok, QueryDSL |

---

## ✨ 기능 목록

| 도메인 | 내용 |
|---|---|
| 🔐 인증 | 회원가입(CUSTOMER/OWNER), 로그인(JWT 발급), 내 정보 조회/수정, 비밀번호 변경, 탈퇴 |
| 🏪 가게 | 등록 → 관리자 승인, 카테고리/지역/키워드 검색, 영업 상태 관리 |
| 🍔 메뉴 | 등록/수정/삭제, 품절·숨김 처리, AI 설명 자동 생성(프롬프트 기반) |
| 🛒 장바구니 | 담기, 수량 변경, 삭제 |
| 📦 주문 | 생성, 상태 변경(요청→수락→조리완료→배송중→배송완료), 5분 이내 취소 |
| 💳 결제 | 생성, 취소 (PG 미연동, 결제 데이터만 관리) |
| ⭐ 리뷰 | 배송완료 주문에 한해 작성, 평점(1~5점), 가게별 평균 평점 노출 |
| 🛡️ 관리자(매니저) | 가게 승인/제재, 지역·카테고리 관리, 전체 데이터 조회 |
| 👑 관리자(마스터) | 매니저 계정 생성/조회/수정/삭제 |

---
## ✅ 구현 기능 체크리스트

### 필수 기능

| 항목 | 구현 여부 |
|---|---|
| 전 도메인 CRUD + Search(페이지네이션, 생성일순 정렬 기본) | ✅ |
| 회원가입 (아이디/비밀번호 형식 검증, 권한 4단계) | ✅ |
| 로그인 (JWT 발급) | ✅ |
| 컨트롤러 엔드포인트 접근 권한/로그인 체크 | ✅ |
| AI API 연동 (Gemini, 메뉴 설명 자동 생성) | ✅ |
| AI 입력 글자수 제한 | ✅ |
| 클라우드 배포 | ⬜ |
| 리뷰 및 평점 기능 (평균 평점 조회, N+1 방지) | ✅ |
| API 문서화 (Swagger) | ✅ |
| Repository/Service 단위 테스트 (성공/실패 케이스) | ✅ (일부 도메인) |

### 도전 기능

| 항목 | 구현 여부 |
|---|---|
| QueryDSL 복합 검색 | ⬜ |
| 로깅 (Logback) | ✅ |
| AI 기능 고도화 | ⬜ |

---

## 🔐 인증 · 권한 구조

- **역할(Role) 4단계**: `CUSTOMER` / `OWNER` / `MANAGER` / `MASTER`
- **인증 방식**: Spring Security + JWT, 세션을 사용하지 않는 STATELESS 구조로 매 요청마다 토큰으로 인증
- **회원가입 제한**: 회원가입 API로는 CUSTOMER/OWNER만 생성 가능, MANAGER는 MASTER가 별도 API로 생성
- **권한 검사 방식**: URL 단위가 아니라 컨트롤러 메서드마다 `@PreAuthorize`를 붙여 세밀하게 분리
- **공개 API**: 가게/카테고리/지역/메뉴/리뷰의 조회(GET)는 비회원도 접근 가능하도록 별도로 허용
- **감사(Audit) 자동 기록**: `AuditorAware` 구현체가 JWT로 인증된 사용자 정보를 읽어 생성/수정/삭제 주체를 엔티티에 자동으로 채움

---

## 🏗 인프라 설계도

`docs/infra.png` 경로에 이미지를 추가하면 아래에 표시됩니다.
![인프라 설계도](./docs/infra.png)

---

## 💡 구현하면서 신경 쓴 부분

모든 테이블에 생성/수정/삭제 시각과 수행자를 기록하는 감사(Audit) 필드를 공통 상위 클래스(`BaseEntity`)로 묶어 관리했습니다. 삭제는 물리 삭제 대신 `deleted_at` 값을 채우는 방식으로 처리해 데이터가 실제로는 남아있도록 했습니다.

엔티티는 `setter`를 열어두지 않고, `create()`나 `update()`처럼 의도가 드러나는 메서드로만 상태를 바꾸도록 했습니다.

가게 목록에서 평균 평점을 보여줄 때, 가게마다 리뷰 테이블을 따로 조회하면 N+1 문제가 생기기 때문에 여러 가게의 평점을 한 번의 쿼리로 묶어 가져오는 방식을 썼습니다.

---

## 🔧 협업 과정 중에 발생한 이슈

- **권한 미들웨어 순서 문제**: Spring Security의 URL 매칭 규칙과 컨트롤러의 `@PreAuthorize`는 서로 다른 단계에서 동작합니다. 공개로 열어야 할 조회 API를 URL 매칭 단계에서 빠뜨리면, 컨트롤러의 권한 설정과 무관하게 로그인 요청부터 막히는 문제가 있어 전체 API를 재점검하고 정리했습니다.
- **SQL 정의 파일과 엔티티 불일치**: 테이블 생성 SQL 파일 일부가 엔티티 필드와 어긋나 있어(컬럼 누락, 테이블명 오기입) 서버 기동이 실패하거나 저장 시점에 오류가 발생하는 문제가 있었고, 전체 테이블을 엔티티 기준으로 재검증해 맞췄습니다.
- **multipart 요청 파트 이름 불일치**: 이미지와 함께 등록하는 API(가게, 메뉴)에서 도메인마다 요청 파트 이름이 다르게 구현되어 있어 혼선이 있었고, 팀 컨벤션으로 통일했습니다.
- **예외 로깅 누락**: 전역 예외 처리기에서 로그를 남기지 않아 서버 오류 발생 시 원인 파악이 어려웠던 부분을 로깅 추가로 개선했습니다.

---

## 🗂 ERD

(https://app.notion.com/p/ERD-3954a3fc0c598096b69fee8e8642d850?source=copy_link)

---

## 🚀 실행 방법

**1. 클론**
```bash
git clone https://github.com/poppq03/404-found-delivery.git
cd 404-found-delivery
```

**2. 로컬 DB 실행 (Docker)**
```bash
docker compose up -d
```

**3. 환경 변수 설정**

`application-local.yaml`에 DB 접속 정보, JWT 시크릿, Gemini API 키, S3 관련 값을 채웁니다.

**4. 애플리케이션 실행**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 🤝 협업 방식

이슈를 먼저 만들고 그에 맞는 브랜치(`feature/`, `fix/`)를 파서 작업한 뒤 PR로 develop에 병합합니다. 커밋 메시지는 `타입: 내용 (#이슈번호)` 형식을 따릅니다.
