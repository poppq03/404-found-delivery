# 🍽️ 404-found-delivery

배달 음식 주문 관리 플랫폼 백엔드 프로젝트입니다. 스파르타코딩클럽 Java 단기심화 부트캠프 5인 팀 프로젝트로 진행했습니다.

## 프로젝트 소개

광화문 근처 음식점을 대상으로 한 배달 주문 관리 플랫폼입니다. 회원가입부터 가게 등록, 메뉴 관리, 장바구니, 주문, 결제, 리뷰까지 배달 서비스의 전체 흐름을 백엔드 API로 구현했습니다.

## 팀원 및 담당 도메인

| 이름 | 담당 도메인 |
| --- | --- |
| 민지 | User, Global (공통 설정, 보안, 예외처리) |
| 서인 | Store, Category, Region |
| 초인 | Menu, AiRequest, Cart, CartItem |
| 우현 | Order, OrderItem, Address |
| 제희 | Payment, Review |

## 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3
- **Security**: Spring Security, JWT (STATELESS)
- **DB**: PostgreSQL, Spring Data JPA / Hibernate 6
- **API 문서화**: Swagger (springdoc-openapi)
- **AI 연동**: Gemini API (메뉴 설명 자동 생성)
- **이미지 저장**: AWS S3
- **기타**: Lombok, QueryDSL

## 주요 기능

- 회원가입 / 로그인 (JWT 기반 인증, 권한: CUSTOMER / OWNER / MANAGER / MASTER)
- 가게 등록 및 승인, 카테고리·지역별 조회 및 검색
- 메뉴 등록/수정/삭제, AI 기반 메뉴 설명 자동 생성
- 장바구니 담기 및 수량 조절
- 주문 생성 → 수락 → 조리완료 → 배송중 → 배송완료, 5분 이내 주문 취소
- 결제 생성 및 취소 (PG 미연동, 결제 내역만 관리)
- 주문 완료 후 리뷰 및 평점 작성, 가게별 평균 평점 조회
- 관리자(MANAGER/MASTER) 전체 가게·주문·유저 관리

## API 문서

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 실행 방법

```bash
git clone https://github.com/poppq03/404-found-delivery.git
cd 404-found-delivery

# 로컬 DB(PostgreSQL) Docker로 실행
docker compose up -d

# 애플리케이션 실행 (local 프로파일)
./gradlew bootRun --args='--spring.profiles.active=local'
```

`.env` 또는 `application-local.yaml`에 DB 접속정보, JWT_SECRET, Gemini API 키, AWS S3 키를 설정해야 합니다.

## 브랜치 전략

`main` / `develop` 기반 Git Flow, 기능별 `feature/`, 버그 수정 `fix/` 브랜치를 사용하며 PR을 통해 develop에 병합합니다.

## 커밋 컨벤션
`feat`, `fix`, `refactor`, `docs`, `test`, `chore` 타입을 사용합니다.
