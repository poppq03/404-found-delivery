# 🍽️ 404-found-delivery

배달 음식 주문 관리 플랫폼 백엔드 프로젝트

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [프로젝트 목적 및 상세](#프로젝트-목적-및-상세)
- [팀원 및 담당 도메인](#팀원-및-담당-도메인)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [ERD](#erd)
- [인프라 설계도](#인프라-설계도)
- [서비스 구성 및 실행 방법](#서비스-구성-및-실행-방법)
- [API 문서](#api-문서)
- [프로젝트 구조](#프로젝트-구조)
- [브랜치 전략 및 커밋 컨벤션](#브랜치-전략-및-커밋-컨벤션)

---

## 프로젝트 소개

광화문 근처 음식점을 대상으로 한 온라인 배달 주문 관리 플랫폼입니다. 고객의 회원가입부터 가게 탐색, 주문, 결제, 리뷰 작성까지의 전체 흐름과, 가게 사장님의 가게·메뉴 관리 및 주문 처리 흐름을 백엔드 API로 구현했습니다.

## 프로젝트 목적 및 상세

- **주제**: 배달 주문 관리 플랫폼 개발
- **목표**: 광화문 근처에서 운영되는 음식점들의 배달 주문 관리, 결제, 주문 내역 관리 기능 제공
- **운영 범위**: 전국 단위로 확장 가능한 구조로 설계하되, 초기에는 광화문 지역 한정 운영
- 사용자 역할(CUSTOMER, OWNER, MANAGER, MASTER)에 따른 권한 분리 및 JWT 기반 인증/인가 구현
- 외부 AI API(Gemini) 연동을 통한 메뉴 설명 자동 생성 기능 포함
- 모든 도메인에 대해 CRUD 및 검색/페이지네이션 기능을 일관되게 제공

(※ 여기에 프로젝트 기획 배경, 팀에서 정한 세부 목표 추가 작성 필요)

## 팀원 및 담당 도메인

| 이름 | 담당 도메인 | GitHub |
| --- | --- | --- |
| 민지 | User, Global (공통 설정/보안/예외처리) | |
| 서인 | Store, Category, Region | |
| 초인 | Menu, AiRequest, Cart, CartItem | |
| 우현 | Order, OrderItem, Address | |
| 제희 | Payment, Review | |

(※ GitHub 프로필 링크 각자 채워넣기)

## 주요 기능

### 사용자
- 회원가입 / 로그인 (JWT 기반 인증)
- 내 정보 조회 및 수정, 비밀번호 변경, 회원 탈퇴
- 권한: CUSTOMER, OWNER, MANAGER, MASTER

### 가게 / 카테고리 / 지역
- 가게 등록 및 관리자 승인, 상태 관리(영업중/휴게시간/준비중 등)
- 카테고리별 · 지역별 · 키워드 가게 검색 (페이지네이션)
- 카테고리/지역 CRUD 및 검색

### 메뉴
- 메뉴 등록/수정/삭제, 이미지 업로드(S3)
- 품절/숨김 처리
- AI(Gemini) 기반 메뉴 설명 자동 생성

### 장바구니 / 주문
- 장바구니 담기, 수량 변경, 삭제
- 주문 생성 → 수락 → 조리완료 → 배송중 → 배송완료
- 주문 생성 후 5분 이내 취소 가능

### 결제
- 결제 생성 및 취소 (PG 미연동, 결제 데이터만 관리)

### 리뷰
- 배송완료 주문에 한해 리뷰 및 평점(1~5점) 작성
- 가게별 평균 평점 조회 (N+1 방지 처리)
- 리뷰 숨김 처리

### 공통
- 모든 도메인 Soft Delete 및 생성/수정/삭제 감사(Audit) 필드 관리
- 전역 예외 처리 및 공통 응답 포맷(`ApiResponse`)

## 기술 스택

| 구분 | 스택 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Security | Spring Security, JWT (STATELESS) |
| DB | PostgreSQL |
| ORM | Spring Data JPA, Hibernate 6 |
| 빌드 도구 | Gradle |
| API 문서화 | Swagger (springdoc-openapi) |
| AI 연동 | Gemini API |
| 이미지 저장소 | AWS S3 |
| 기타 | Lombok, QueryDSL |
| 협업 도구 | GitHub, Notion |

## ERD

![ERD](./docs/erd.png)

(※ ERD 이미지 파일을 `docs/erd.png` 경로에 추가해주세요. dbdiagram.io 또는 diagrams.net에서 export)

## 인프라 설계도

![Infra](./docs/infra.png)

(※ 인프라 설계도 이미지를 `docs/infra.png` 경로에 추가해주세요)

## 서비스 구성 및 실행 방법

### 요구 사항
- JDK 17
- Docker (로컬 PostgreSQL 실행용)

### 실행 방법

```bash
git clone https://github.com/poppq03/404-found-delivery.git
cd 404-found-delivery

# 1. 로컬 DB(PostgreSQL) Docker로 실행
docker compose up -d

# 2. 환경변수 설정
# application-local.yaml.example 참고하여 application-local.yaml 작성
# (.env 사용 시 .env.example 참고)
# 필요한 값: DB 접속정보, JWT_SECRET, GEMINI_API_KEY, AWS S3 키

# 3. 애플리케이션 실행 (local 프로파일)
./gradlew bootRun --args='--spring.profiles.active=local'
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

## API 문서

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

(※ 배포 후 실제 서버 주소로 교체 필요)

## 프로젝트 구조
src/main/java/com/found404/delivery
├── domain
│   ├── user
│   ├── store
│   ├── category
│   ├── region
│   ├── menu
│   ├── cart
│   ├── cartitem
│   ├── order
│   ├── orderitem
│   ├── address
│   ├── payment
│   ├── review
│   └── airequest
├── global
│   ├── config
│   ├── entity
│   ├── exception
│   ├── response
│   ├── security
│   └── storage
└── sql

각 도메인은 `entity / repository / service / controller / dto` 구조로 구성됩니다.

## 브랜치 전략 및 커밋 컨벤션

- `main` / `develop` 기반 운영, 기능 단위 `feature/`, 버그 수정 `fix/` 브랜치 사용
- 이슈 생성 → 브랜치 생성 → 커밋 → PR → 코드리뷰 → develop 병합 순서로 진행
- 커밋 메시지 형식: `타입: 내용 (#이슈번호)` (예: `feat: 메뉴 등록 기능 구현 (#12)`)
