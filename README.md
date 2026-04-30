# 🎰 로또 최적 번호 생성 시스템 (Lotto Optimization System)

Java 21 가상 스레드(Virtual Threads)와 Spring Boot 4 기반의 로또 번호/티켓 생성 서비스입니다.

## 🚀 주요 기능
- 역대 당첨 번호 수집: 동행복권 API 조회 기반으로 이력 데이터를 수집합니다.
- 가상 스레드 병렬 처리: 대량 회차 조회 시 I/O 효율을 높입니다.
- 캐시 최적화: 회차 응답/최신 회차/이력 집합을 분리 캐싱해 재호출 비용을 줄입니다.
- 중복 제외 생성: 역대 당첨 조합과 중복되지 않는 번호 생성 모드를 제공합니다.
- 티켓 발권 API: 수동/자동 조합, 영수증 포맷 응답을 제공합니다.
- ProblemDetail 오류 표준화: RFC 7807 형식으로 일관된 오류 응답을 반환합니다.

## 🛠 기술 스택
- Language: Java 21
- Framework: Spring Boot 4.0.6
- Build Tool: Gradle 9.4.1 (Wrapper)
- Core: Spring Web, Validation, Cache, Actuator, RestClient
- Cache: Caffeine
- Resilience: Spring Retry (`RetryTemplate`)
- Test: JUnit 5, Mockito, MockMvc

## 🏗 프로젝트 구조
```text
com.lotto
├── client      # 외부 API 통신 추상화/구현
├── controller  # REST API 엔드포인트 + 예외 처리
├── service     # 비즈니스 로직/탐색/이력 수집/캐시
├── domain      # 도메인 모델 (record, enum)
└── config      # 속성 바인딩, RestClient, Cache 설정
```

## 🏃 실행 방법 (Windows PowerShell)
### 로컬 실행
```powershell
.\gradlew.bat bootRun
```

### API 호출 예시
- 번호 생성: `GET http://localhost:8080/api/lotto/generate?count=5`
- 티켓 발권: `GET http://localhost:8080/api/lotto/ticket?games=5&skipHistory=true`

## 🧪 테스트 실행
```powershell
.\gradlew.bat test
```

## 📝 설계 원칙
- SRP: API 통신, 탐색, 이력 수집, 생성, 발권 책임을 분리
- DIP: `LottoApiClient` 인터페이스 기반 외부 연동 추상화
- OCP: 외부 API 어댑터 교체 시 서비스 계층 변경 최소화

---
본 프로젝트는 학습 및 실무 최적화 예시용으로 제작되었습니다.
