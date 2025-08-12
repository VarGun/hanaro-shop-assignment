## Hanaro 쇼핑몰 백엔드 (Spring Boot)

Spring Boot 3 기반의 쇼핑몰 REST API입니다. 사용자/인증, 상품, 장바구니, 주문, 관리자 통계를 제공합니다.

### 기술 스택

- Spring Boot (Web, Validation, Security, Data JPA, Actuator)
- springdoc-openapi (Swagger UI)
- DB: H2(기본), MySQL(로컬 프로필)
- Auth: JWT Bearer (jjwt)
- Gradle, Java 17, Lombok

### 요구 사항

- Java 17 이상
- Gradle Wrapper 포함(별도 설치 불필요)
- (선택) MySQL 8.x – 로컬 프로필 사용 시

### 실행 (local 프로필 전용)

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
# 애플리케이션: http://localhost:8080
# Swagger UI:    http://localhost:8080/swagger-ui/index.html
```

### MySQL 로 실행 (local 프로필)

1. DB 초기화 (필수)

```bash
mysql -u root -p
DROP DATABASE IF EXISTS hanarodb;
CREATE DATABASE hanarodb;
exit
```

2. 애플리케이션 실행

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

`src/main/resources/application-local.properties`에 MySQL 연결 정보와 JPA/로깅 옵션이 설정되어 있습니다.

### API 문서 (Swagger)

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI 그룹
  - public: `/api/**` (관리자 경로 제외)
  - admin: `/api/admin/**`
- JWT 필요 엔드포인트는 Swagger의 Authorize 버튼으로 `Bearer <token>` 입력 후 호출합니다.

### Actuator

- 기본 경로: `/actuator/**`
- 로컬 프로필(local) 노출 엔드포인트(`application-local.properties`): `health`, `info`, `metrics`, `loggers`, `mappings`
- 보안
  - Public 허용: `/actuator/health`, `/actuator/metrics/**`
  - 그 외(`/actuator/mappings`, `/actuator/loggers`, `/actuator/info` 등)는 인증 필요(JWT)
- Health 상세: `management.endpoint.health.show-details=always`
- 예시
  - `GET /actuator/health`
  - `GET /actuator/metrics`
  - `GET /actuator/metrics/http.server.requests`
  - `GET /actuator/mappings`
  - `GET /actuator/loggers`
- 노출 확장(개발용 권장): `management.endpoints.web.exposure.include=*`

### 인증/권한

- 회원가입: `POST /api/auth/signup`
- 로그인: `POST /api/auth/login` → `{ accessToken, tokenType: "Bearer" }`
- 내 정보: `GET /api/auth/me` (Authorization 헤더 필요)
- 관리자 로그인: `POST /api/auth/admin/login`
  - 요청 JSON의 `email` 값 뒤에 서버에서 `@hanaro.com`을 자동 부착합니다. (예: `{"email":"admin","password":"..."}` → DB 이메일은 `admin@hanaro.com`)
- 권한 정책
  - Public: 로그인/회원가입, 상품 조회, Swagger/Actuator 일부
  - USER/ADMIN: 장바구니/주문/프로필 수정
  - ADMIN: `/api/admin/**`

### 샘플 로그인 계정

- 일반 사용자: `POST /api/auth/login`

```json
{
  "email": "user1@aaa.com",
  "password": "Passw0rd!"
}
```

- 관리자: `POST /api/auth/admin/login`

```json
{
  "email": "hanaro",
  "password": "12345678"
}
```

- 비고: 이메일 Validation 정책 때문에 관리자 로그인은 `email` 필드에 아이디만 입력해도 서버에서 `@hanaro.com`을 자동으로 붙여 처리합니다.

시드 데이터(`data.sql`)에 관리자가 포함됩니다. 비밀번호는 해시로 저장되므로 바로 로그인되지 않을 수 있습니다. 필요 시 회원가입 후 DB에서 해당 사용자의 `role`을 `ADMIN`으로 변경하거나, 관리자 계정의 비밀번호를 원하는 bcrypt 해시로 갱신해 사용하세요.

### 주요 엔드포인트 요약

- Public
  - `POST /api/auth/signup`, `POST /api/auth/login`, `POST /api/auth/admin/login`
  - `GET /api/products`, `GET /api/products/{id}`
- USER (인증 필요)
  - 장바구니: `POST/GET/PATCH/DELETE /api/cart/**`
  - 주문: `POST /api/orders`, `GET /api/orders`, `GET /api/orders/{id}`, `PATCH /api/orders/{id}/cancel`
  - 프로필: `PATCH /api/users/{id}`
- ADMIN (관리자 권한)
  - 상품: `POST/PUT/DELETE/PATCH /api/admin/products/**` (이미지 업로드)
  - 주문: `GET /api/admin/orders` (검색/필터)
  - 사용자: `GET/DELETE /api/admin/users/**`
  - 통계: `GET /api/admin/stats/sales`, `GET /api/admin/stats/products`

### 파일 업로드

- 정적 노출: `GET /uploads/**` → 실제 경로는 `application.properties`의 `app.file.upload-dir`
- 용량 제한
  - 기본: 파일당 10MB, 요청당 10MB
  - local 프로필: 파일당 512KB, 요청당 3MB (관리자 상품 생성/수정 시 총량 검사 포함)
- 업로드 샘플(관리자 상품 생성)

```bash
curl -X POST http://localhost:8080/api/admin/products \
  -H "Authorization: Bearer <TOKEN>" \
  -F name="MacBook Air" \
  -F price=1700000 \
  -F description="M3" \
  -F image=@/path/to/image.jpg
```

#### 정적 파일 경로 구분

- `resources/static/origin`

  - 목적: 샘플/시드용 원본 이미지 보관(배포 아티팩트에 포함)
  - URL: `/origin/**` (예: `/origin/image1.jpg`)
  - 특성: 읽기 전용(코드/데이터 배포 시 함께 제공), `data.sql`에서 초기 이미지 경로로 사용

- `resources/static/upload`

  - 목적: 관리자 업로드 이미지 저장(날짜별 디렉토리 `yyyy/MM/dd`)
  - URL: `/upload/**` (예: `/upload/2025/08/12/<uuid>.jpg`)
  - 특성: 런타임에 파일이 추가됨. `FileStorageService`에서 저장 루트(`app.file.upload-root`)로 사용

- 참고: `WebConfig`는 별도로 파일시스템 디렉토리(`app.file.upload-dir`)를 `/uploads/**`로 노출합니다. 현재 업로드 서비스는 `/upload/**` 경로를 반환하므로, 정적 리소스(`/resources/static/upload`)와 파일시스템(`/uploads/**`) 두 경로가 공존합니다.

### 데이터베이스

- 스키마: `src/main/resources/schema.sql`
- 시드: `src/main/resources/data.sql`
- JPA: `ddl-auto=update`, `show-sql=true`(local)

### 로깅

- 설정: `src/main/resources/logback-spring.xml`
- 파일 출력
  - `logs/application.log`
  - 비즈니스 로그: `logs/business_order.log`, `logs/business_product.log`

### 디렉터리

```
src/main/java/com/example/hanaro
  ├─ config/        # Security, Swagger, WebConfig
  ├─ controller/    # REST 컨트롤러
  ├─ dto/           # 요청/응답 DTO
  ├─ entity/        # JPA 엔티티
  ├─ repository/    # Spring Data JPA 리포지토리
  ├─ security/      # JWT, UserDetails
  └─ service/       # 비즈니스 로직
src/main/resources
  ├─ application.properties
  ├─ application-local.properties
  ├─ schema.sql / data.sql
  └─ logback-spring.xml
```

### 트러블슈팅

- Swagger 호출이 401이면: Authorize 버튼으로 `Bearer <token>` 등록 후 재시도
- 파일 업로드 오류
  - 512KB(파일당) 또는 3MB(요청당) 초과 시 400 응답과 한글 에러 메시지 반환
  - local 프로필 기준. 기본 프로필은 10MB
- MySQL 연결 오류
  - DB/계정/권한 확인, `application-local.properties`의 URL/계정/비밀번호 확인

### 요구 사항 대응 체크리스트

- **관리자(Admin) 기능**

  1. 상품 등록/조회/수정/삭제(이미지 포함)
     - 엔드포인트: `POST/PUT/DELETE /api/admin/products/**`, `PATCH /api/admin/products/{id}/stock`
     - 구현: `controller/AdminProductController`, `service/ProductService`, `service/storage/FileStorageService`
  2. 재고 수량 조정
     - 엔드포인트: `PATCH /api/admin/products/{id}/stock`
  3. 주문 내역 조회(검색)
     - 엔드포인트: `GET /api/admin/orders` (status, fromDate, toDate, keyword)
     - 구현: `controller/AdminOrderController`, `service/AdminOrderService`
  4. 매출 통계 조회
     - 엔드포인트: `GET /api/admin/stats/sales`, `GET /api/admin/stats/products`
     - 구현: `controller/AdminStatController`, 통계 테이블 `daily_sales_stat`, `daily_product_stat`
  5. 회원 관리(목록/검색/삭제)
     - 엔드포인트: `GET /api/admin/users?q=`, `DELETE /api/admin/users/{id}`
     - 구현: `controller/AdminUserController`, `service/AdminUserService`

- **일반 사용자(User) 기능**

  1. 회원 가입/로그인
     - 엔드포인트: `POST /api/auth/signup`, `POST /api/auth/login`, `POST /api/auth/admin/login`
  2. 상품 목록 조회(검색)
     - 엔드포인트: `GET /api/products?q=`
  3. 상품 상세 보기
     - 엔드포인트: `GET /api/products/{id}`
  4. 장바구니 담기/수정/삭제
     - 엔드포인트: `POST/GET/PATCH/DELETE /api/cart/**`
     - 구현: `controller/CartController`, `service/CartService`
  5. 주문 생성(장바구니 기반)
     - 엔드포인트: `POST /api/orders`
  6. 내 주문 내역 확인
     - 엔드포인트: `GET /api/orders`, `GET /api/orders/{id}`, `PATCH /api/orders/{id}/cancel`

- **공통 기능**
  - Validation: 회원가입/로그인/상품 등록 요청 등에 `@Valid`/`@Validated` 적용, 필드별 메시지 응답. 예) `dto/UserSignUpRequest`, `dto/ProductCreateRequest`, `controller/GlobalExceptionController`
  - Exception 처리: `GlobalExceptionController`에서 공통 에러 메시지 구조(400/401/403/404/409/500) 반환
  - Batch & Scheduler: 주문 상태 전이(5분/15분/1시간), 일별 통계 집계(매일 00:00) — `service/OrderService`의 `@Scheduled` 메서드
  - DB 및 테스트 코드: 개발 단계 데이터 export(`schema.sql`, `data.sql`) 제공, `data.sql` 기반 초기 데이터 삽입 확인
  - 성능 모니터링/상태 점검: Spring Boot Actuator — `GET /actuator/health`, `GET /actuator/metrics/**` 등

### 평가 항목 대응 가이드

- **인증/인가 (JWT, ROLE 기반 접근 제어)**

  - JWT 발급/검증: `security/JwtTokenProvider`, `security/JwtAuthenticationFilter`
  - 사용자/관리자 구분 및 접근 제한: `config/SecurityConfig`의 `@EnableMethodSecurity` + `requestMatchers`, 컨트롤러의 `@PreAuthorize`
  - 확인 방법: 로그인 후 `Authorization: Bearer <token>`로 USER 엔드포인트 접근 가능, `GET /api/admin/...`은 ADMIN만 허용

- **Swagger (OpenAPI 3)**

  - 설정: `config/SwaggerConfig` (public/admin 그룹 분리)
  - 확인 방법: `http://localhost:8080/swagger-ui/index.html`에서 모든 API 테스트 가능, 그룹 탭 전환으로 사용자/관리자 API 구분

- **파일 업로드**

  - 날짜별 경로 저장: `service/storage/FileStorageService` → `/upload/yyyy/MM/dd/<uuid>.(jpg|png)` 경로 생성 및 저장
  - 파일명 중복 방지: UUID 파일명 사용
  - 크기/형식 제한: 파일당 512KB, 총 3MB(관리자 상품 등록/수정 시 총량 검사), 허용 형식 `image/jpeg`, `image/png`
  - 정적 제공: `config/WebConfig`의 `/uploads/**` 리소스 핸들러 및 `application.properties`의 `app.file.upload-dir`

- **Validation**

  - 적용 위치: `@Valid`/`@Validated` (예: `AuthController`, `AdminProductController`, `CartController`, `UserController`)
  - 에러 메시지: `controller/GlobalExceptionController`에서 형식 표준화(400/401/403/404/409 등)

- **스케줄링 (상태 전이 + 집계)**

  - 5분 주기: 결제 완료(ORDERED) → 배송 준비(READY) `OrderService.moveOrderedToReady()`
  - 15분 주기: 배송 준비(READY) → 배송 중(SHIPPING) `OrderService.moveReadyToShipping()`
  - 1시간 주기: 배송 중(SHIPPING) → 배송 완료(COMPLETED) `OrderService.moveShippingToCompleted()`
  - DB 반영 확인: 각 메서드 실행 시 로그(`[SCHED] ... updated=<건수>`) 및 상태 변경 SQL 결과로 확인

- **배치 Job (일자별 통계)**

  - 매일 00:00 전일 통계 적재: `OrderService.collectDailyStats()`
  - 저장 테이블: `daily_sales_stat`, `daily_product_stat` (스키마 `schema.sql`)
  - 조회 API: 관리자 통계 `GET /api/admin/stats/sales`, `GET /api/admin/stats/products`

- **Actuator**

  - 노출 엔드포인트: `/actuator/health`, `/actuator/metrics/**`, `/actuator/mappings`, `/actuator/loggers`, `/actuator/info`(local 기준)
  - 보안: `health`, 일부 `metrics` 공개, 나머지는 인증 필요

- **로깅**

  - 콘솔 + 파일 분리, 롤링: `logback-spring.xml`
  - 비즈니스 전용 로거: `business.order`, `business.product` (예: 주문 생성/취소 및 재고 증감 로그)
  - 로그 디렉터리: `logs/` 하위에 일자별 분리 저장

- **코드 품질/구조화**

  - 계층 분리: `controller` / `service` / `repository` / `dto` / `entity` / `security` / `config`
  - 중복 최소화: DTO 변환 정적 팩토리(`OrderResponse.from` 등), 서비스 단 위임, 글로벌 예외 처리로 응답 형식 일원화
  - 네이밍/로직: 명확한 메서드명과 가드절, 트랜잭션 범위 명시(`@Transactional`)

- **기본 기능 구현(관리자/사용자)**
  - 관리자: 상품 등록/수정/삭제/재고변경, 주문 검색, 사용자 검색/삭제, 통계 조회
  - 사용자: 회원가입/로그인, 상품 조회, 장바구니 추가/수정/삭제, 주문 생성/조회/취소, 프로필 수정
