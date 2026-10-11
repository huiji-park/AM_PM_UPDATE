# AM_PM MVP — 테스트 계획

- 작성일: 2026-10-03 (Asia/Seoul)
- 상태: 실행 전 테스트 설계, 사용자 검토용
- 목표: 핵심 사용자 여정·권한 격리·데이터 원자성·실패 복구를 재현 가능한 검증으로 증명한다.
- 기준: [상세 설계 0.2](../specs/2026-10-03-am-pm-mvp-design.md), [API 계약 0.2](../specs/2026-10-03-am-pm-mvp-api-contract.md), [화면 명세 0.2](../specs/2026-10-03-am-pm-mvp-screens-verification.md), [제약 검토](../specs/2026-10-03-am-pm-mvp-constraints-review.md)

이 문서는 제품 구현 계획이나 실행 결과가 아니다. 테스트 대상·환경·데이터·순서·예상 결과·종료 조건을 정의한다. 아래 파일·명령은 구현 단계에 마련할 인터페이스이며 현재 존재하거나 실행 가능한 것으로 주장하지 않는다. 테스트 작성·실행은 제품 구현 계획에서 해당 기능의 작업과 함께 배정한다.

## 1. 범위와 증거의 경계

기존 AU 15개, PM 18개, TK 15개, QF 20개로 구성된 68개 ID를 유지한다. 이번 제약 검토에서 RV 18개를 추가하여 기능 검증 추적 ID는 총 86개다. 별도로 핵심 E2E 여정 5개와 성능 실험 4개를 운영한다. 관련 ID를 한 테스트가 함께 검증할 수 있으므로 테스트 함수 개수와 ID 개수는 같지 않아도 된다.

화면 시안은 탐색·레이아웃 검토에만 사용한다. 제품 E2E는 실제 Vue 빌드·FastAPI·PostgreSQL을 연결해야 한다. API를 모킹한 화면 테스트는 컴포넌트 테스트로 분류하고 E2E 통과로 기록하지 않는다.

이메일 검증·계정 복구·채팅·간트·파일 업로드는 범위 밖이다. 구현되지 않은 기능의 테스트를 skipped로 추가해 통과율을 높이지 않는다. OpenAPI 문서 경로는 환경 검증에 포함하되 기존 21개 업무·health 엔드포인트 수를 바꾸지 않는다.

## 2. 우선순위와 실행 순서

### 2.1 기본 분류

기능 ID 86개 모두 MVP 수용 대상이다. 우선순위는 실행 순서와 결함 대응 속도를 정하며 낮은 우선순위라는 이유로 누락하지 않는다.

| 분류 | ID | 핵심 관찰 |
|---|---|---|
| P0 인증·격리 | AU-01·02·05~12·15, PM-01~05·07~18 | 비밀 미노출, 비소속 차단, 소유자 1명, 원자성 |
| P0 업무 무결성 | TK-02·03·08~14 | 복합 FK, 현재 소속, 버전 충돌, 올바른 삭제 권한 |
| P0 프론트 경계 | QF-13·14·18 | 외부 리다이렉트·스크립트 실행·계정 정보 잔류 차단 |
| P0 검토 보완 | RV-04·06·09·10·12·14·15·16·18 | 신뢰 경계, 테스트 DB 격리, 읽기·쓰기 수명, 운영 보호 |
| P1 기능·복구 | 위에 없는 기존·RV ID 전부 | 입력 경계, 필터, 오류 UX, 환경 호환 |
| P2 측정·사용성 | PERF-01~04, 수동 시각 점검 | 성능 한계와 실제 사용자 조작 확인 |

### 2.2 단계별 통과 게이트

| 게이트 | 착수 조건 | 실행 | 통과 조건 |
|---|---|---|---|
| G0 환경 | 제품 실행 환경·의존성 lock 마련 | 런타임·DB·마이그레이션·설정 확인 | 독립 DB·앱·프록시·브라우저 구동, readiness 정상 |
| G1 단위·계약 | 해당 모델·스키마·규칙 구현 | 입력·날짜·상태·권한·OpenAPI 검사 | 기대값 일치, 정의하지 않은 필드 거부 |
| G2 API·DB | 인증·프로젝트·업무 API 구현 | PostgreSQL 통합·실제 commit·동시성·실패 주입 | P0 포함 모든 구현 기능의 시나리오 통과 |
| G3 프론트 | 화면·공통 API 래퍼 구현 | 컴포넌트·라우팅·오류·초안 보존 | API mock 상태를 정확히 표현, 늦은 응답 폐기 |
| G4 제품 연결 | G2·G3 통과 | E2E 5개, 브라우저·반응형·키보드 | 실제 UI→API→DB→재조회 흐름 성공 |
| G5 시연 후보 | 변경 동결된 대상 커밋 | 프록시·HTTPS·복구·성능·최종 회귀 | P0/P1 미해결 0, 기능 ID 누락 0, 결과·제약 기록 |

G0가 불충분하면 제품 테스트는 BLOCKED다. 설계 문서 검증은 계속 가능하다. 설치·환경 구성은 별도 작업으로 추적하며 외부 환경을 임의로 변경하지 않는다.

## 3. 환경 설계

### 3.1 환경별 구성

| 환경 | 구성 | 목적 |
|---|---|---|
| Unit | Python, DB 없는 규칙 테스트 | 빠른 경계값 검사 |
| Integration | FastAPI + 독립 PostgreSQL 18 | FK·트랜잭션·인증·API 검증 |
| Component | Vue + Vitest + 요청 모킹 | UI 상태·입력·응답 순서 제어 |
| E2E | Vue 빌드 + API + PostgreSQL + 같은 출처 프록시 | 사용자 여정과 쿠키 전달 |
| Proxy-security | HTTPS 테스트 프록시, production과 같은 제한 설정 | Secure 쿠키, Origin, 요청 제한, 본문 제한 |
| Performance | 전용 데이터·전용 프로세스, 디버그 로그 비활성화 | 재현 가능한 지연·처리량 관찰 |

일반 E2E는 인증 요청 제한을 테스트 설정에서 완화할 수 있다. RV-03·04 및 AU-14는 완화하지 않은 독립 프록시 인스턴스에서 실행한다. production 설정에 테스트용 완화가 유입되면 실패다.

브라우저 검증은 잠금 파일로 정한 Playwright 버전과 그 버전에 대응하는 Chromium·Firefox·WebKit을 사용한다. Windows 로컬에서 사용 가능한 엔진을 확인하고, 누락된 엔진은 CI에서 검증한다. WebKit 통과를 실제 iOS Safari 기기 검증과 동일하게 표기하지 않는다.

### 3.2 현재 착수 조건

현재 작업 공간에는 원본 Client/Server와 설계 문서만 있고 신규 frontend/backend는 없다. PowerShell PATH에서 python·uv·docker·psql은 발견되지 않았고 node는 v20.9.0으로 확인했다. Codex 번들 Python은 문서 계산에 사용 가능하지만 제품 런타임 설정은 별도로 필요하다.

- Python 3.13, 선택한 Node LTS, uv·npm 및 잠금 파일 호환성 확인.
- 로컬 또는 CI의 전용 PostgreSQL 18과 disposable DB 생성 권한 확인.
- 프록시와 HTTPS 테스트 환경 준비.
- 빌드·lint·타입 검사·pytest·Vitest·Playwright 실행 스크립트 작성.
- 표준 명령과 설정을 README에 남겨 새 환경에서 재현.

### 3.3 DB 격리·정리 규칙

DB 이름은 `ampm_test_<run_id>_<worker_id>` 형식이며 run_id는 20자 이하 영문 소문자·숫자, worker_id도 제한된 식별자다. 별도 테스트 인스턴스 allowlist, APP_ENV=test, 현재 실행 토큰 세 조건을 모두 검사한다. 이름만 test인 운영 DB를 안전하다고 판단하지 않는다.

provisioner만 DB 생성·제거 권한을 가진다. 앱 테스트 계정은 자기 테스트 DB의 필요한 권한만 가진다. fixture는 실행 중 만든 DB 이름을 manifest에 기록하고 정리 시 exact match만 사용한다. 원본 DB·사용자 지정 다른 DB에 정리 명령을 보내지 않는다.

병렬 worker는 DB를 공유하지 않는다. 한 시나리오의 동시성 재현 연결만 같은 worker DB를 공유한다. 외부 transaction rollback으로 격리하는 테스트와 실제 commit을 검증하는 테스트를 섞지 않는다. 후자는 테스트별 새 DB 또는 명시적으로 초기화한 독립 데이터셋을 사용한다.

## 4. 고정 테스트 데이터

### 4.1 기본 fixture

ID는 결정적인 UUID를 fixture에서 생성·명명하되 실제 사용자 정보를 사용하지 않는다. 비밀번호는 테스트 전용 값을 실행 시 주입하고 해시는 실제 Argon2로 생성한다. 세션 토큰은 실행마다 새로 만들며 필요한 만료 조건만 고정한다.

| 이름 | 내용 |
|---|---|
| A | alice@example.test, 표시 이름 앨리스, P 소유자 |
| B | bora@example.test, 표시 이름 보라, P 팀원 |
| C | chris@example.test, 표시 이름 크리스, Q 소유자, P 비소속 |
| D | dara@example.test, 표시 이름 다라, Q 팀원, P 비소속 |
| E | absent@example.test, 등록되지 않은 조회 대상 |
| P | 소유자 A, 멤버 A/B, 기간 2026-10-01~2026-10-31, version=1 |
| Q | 소유자 C, 멤버 C/D, 기간 없음, version=1 |
| T1 | P, 작성 A, 담당 B, TODO, 마감 2026-10-03, version=1 |
| T2 | P, 작성 B, 담당 A, IN_PROGRESS, 마감 2026-10-02, version=1 |
| T3 | P, 작성 A, 담당 B, DONE, 마감 2026-10-01, 완료 시각 존재 |
| T4 | P, 작성 B, 미배정, TODO, 시작·마감 null |
| T5 | Q, 작성 C, 담당 D, TODO, P 삭제 후에도 보존할 업무 |

기본 Clock은 `2026-10-03T14:59:59Z`로 고정한다. 한국 시각은 10월 3일 23:59:59다. 1초 후에는 10월 4일이므로 T1의 지연 판정이 바뀌어야 한다. 기본 P 완료율은 1/4=25%다. T2만 지연이고 T3는 DONE이라 지연이 아니다.

### 4.2 별도 fixture

- 페이지: 프로젝트 업무 0·1·20·21·100·101개, 동일 created_at인 업무를 포함.
- 팀원 선택: P 멤버 101명 이상, 첫 페이지에 없는 담당자.
- 입력 경계: 이름 0/1/100/101자, 제목 0/1/200/201자, 설명 0/10000/10001자, password 11/12/128/129 코드 포인트.
- 인코딩: 한글·이모지·결합 문자·공백만 입력·CRLF·리터럴 `%`·`_`·역슬래시.
- 버전: 0·1·현재 버전·오래된 버전·bool·실수·문자열·누락.
- 세션: 유효·만료 직전·만료 시각·변조·삭제됨·다른 브라우저.
- 권한: 비로그인·소유자·팀원인 작성자·팀원인 비작성자·비소속·제거 직후 사용자.
- 장애: DB 접속 불가·pool 고갈·lock timeout·commit 실패·응답 유실·늦은 응답.

fixture의 초기 상태는 테스트마다 재생성한다. 한 테스트의 소유권 이전·탈퇴가 다른 테스트 결과를 바꾸지 않아야 한다.

## 5. 기존 68개 시나리오의 배치

세부 입력·기대값은 [화면·검증 명세 12절](../specs/2026-10-03-am-pm-mvp-screens-verification.md)의 ID를 기준으로 하며 아래 배치가 전부를 포함한다.

| ID 범위 | 주 계층 | 계획 파일 | 관찰할 결과 |
|---|---|---|---|
| AU-01~12·15 | API/DB | backend/tests/integration/test_auth.py | HTTP·쿠키·저장된 해시·세션 행 |
| AU-13 | Component | frontend/tests/component/auth-bootstrap.spec.ts | unavailable 상태 유지 |
| AU-14 | Proxy | backend/tests/system/test_auth_rate_limit.py | 공유 zone·429·직접 우회 차단 |
| PM-01~18 | API/DB | backend/tests/integration/test_projects_members.py | 소속·FK·version·원자성 |
| TK-01~11 | API/DB | backend/tests/integration/test_tasks.py | 입력·상태·담당·수정 권한 |
| TK-12~14 | Concurrency | backend/tests/integration/test_concurrency.py | 별도 연결의 최종 commit 상태 |
| TK-15 | API/Component 연결 | frontend/tests/e2e/write-recovery.spec.ts | 응답 유실 후 자동 중복 요청 없음 |
| QF-01~07 | API/DB | backend/tests/integration/test_query_contracts.py | 필터·정렬·snapshot·한국 날짜 |
| QF-08~15·18·20 | Component | frontend/tests/component/query-and-errors.spec.ts | 늦은 응답·충돌·권한·오류 표시 |
| QF-16·17·19 | E2E/수동 | frontend/tests/e2e/layout-accessibility.spec.ts | 실제 이동·직접 진입·키보드·반응형 |

단위 테스트는 날짜 비교·최종 PATCH 병합·권한 predicate·지연 계산을 빠르게 반복한다. 동일 ID의 보안·DB 계약은 단위 테스트만으로 대체하지 않는다. 예를 들어 PM-03의 FK 실패는 실제 PostgreSQL commit에서 확인해야 한다.

## 6. 이번 검토에서 추가한 18개 시나리오

| ID | 준비·실행 | 기대 결과·증거 |
|---|---|---|
| RV-01 | Vite 동일 출처 /api/docs에서 로그인·업무 변경; API 포트 직접 Origin과 production 문서 경로도 검사 | 같은 출처만 성공; 출처 완화 없음; production docs/openapi 404 |
| RV-02 | 이모지 최대 입력을 UTF-8·ASCII escape로 각각 전송; 262144·262145바이트 본문·Content-Length 없는 요청·압축 요청 | 유효 두 인코딩 통과; 경계 이하에서는 스키마 검사 진행, 초과 413; 압축 415; 저장값 동일 |
| RV-03 | 비워진 rate zone에서 같은 IP로 로그인·등록 교대로 7개를 1초 안에 전송 | 최초 1+burst 5 요청은 제한기로 지연하지 않고 upstream 도달, 다음은 429; 둘이 같은 예산 소비 |
| RV-04 | 같은 실제 IP에서 X-Forwarded-For를 매번 변경해 RV-03 반복 | 제한이 새로 생기지 않음; API·DB 포트 외부 비공개; 직접 앱은 프록시 한계를 우회할 수 없게 배포 |
| RV-05 | 앱의 테스트 전용 pool 1/overflow 0을 점유한 뒤 인증·조회 요청, 해제 후 재시도 | 획득 제한 시간 부근에 503 RESOURCE_BUSY, 500·무한 대기 없음; 해제 후 성공·연결 수 회복 |
| RV-06 | DB 이름·호스트 allowlist·APP_ENV·실행 토큰을 각각 잘못 설정한 테스트 bootstrap | DDL·정리 전에 중단; 다른 DB에 변경 없음; 오류에 비밀 연결 문자열 미출력 |
| RV-07 | 유효 인증을 거쳐 쓰기와 오류 요청 반복, auth 조회와 service begin을 계측 | 이미 시작된 transaction 예외 없음; Session 객체 공유 없음; 모든 경로에서 연결 반환 |
| RV-08 | TODO 읽기 후 업무 상태 변경, IN_PROGRESS 응답 수신; 반대 순서의 일시 누락도 구성 | 같은 ID는 가장 큰 version만 한 열에 표시; 누락 가능성 표시·갱신 제공; 쓰기 정지 후 완전 재조회로 수렴 |
| RV-09 | A의 보호 GET 응답을 지연시키고 로그아웃 후 B 로그인, 뒤늦게 A 응답 전달 | A의 내용이 B 화면·캐시에 삽입되지 않음; 인증 세대가 다른 응답 폐기 |
| RV-10 | 읽기 snapshot 생성 후 멤버 제거 commit, 진행 중 읽기 완료와 제거 후 새 읽기를 각각 검사 | 이미 허가된 snapshot 응답은 허용, 새 요청은 404; 기존 읽기를 취소한다고 주장하지 않음 |
| RV-11 | 생성 POST를 실제 commit시킨 뒤 응답만 프록시에서 끊기 | DB에는 1건 존재; 프론트 추가 POST 0회; 재조회 안내; 재조회로 생성 확인 |
| RV-12 | 빈 disposable DB upgrade→fixture→검증; 별도 빈 DB에서 downgrade 한 단계→재upgrade; 시드 2회 | FK·인덱스 복원, 시드 중복 없음, 시드 밖 사용자 데이터 보존; 실제 commit 증거 |
| RV-13 | DB 정상·다운·Alembic head 불일치 상태에서 live/ready 호출 | live 200 유지; ready 정상 200, 나머지 503; 연결 정보 미노출 |
| RV-14 | 성공·401·422·409·500 경로에 표식 비밀번호·쿠키·이메일 넣고 응답·앱/프록시 로그 수집 | password/token/hash/DB URL·원본 query 유출 없음; 앱 request_id 추적 가능 |
| RV-15 | 21개 계약 엔드포인트를 OpenAPI와 대조, 역할별 허용/거부 행렬 적용, 과잉 필드 주입 | 경로·메서드·입출력 일치; owner/role/created_by 주입 거부; 숨은 호환 API 없음 |
| RV-16 | production 설정의 실제 HTTPS 프록시에서 쿠키·SPA 깊은 경로·API 404·직접 포트 접근 검사 | Secure/HttpOnly/Host 규칙 준수; SPA만 fallback; API는 JSON 오류 또는 명시한 프록시 오류; 내부 포트 비공개 |
| RV-17 | Chromium·Firefox·WebKit에서 날짜·한국어 IME·이모지 길이·비밀번호 붙여넣기·Modal 키보드 조작 | 입력이 브라우저마다 누락·변조되지 않음; 조합 중 검색 처리 안정; 키보드 핵심 여정 가능 |
| RV-18 | 대량 담당 해제·프로젝트 삭제에 timeout/오류 주입, 별도 Q에 동시 변경 | P는 전체 rollback 또는 전체 commit; 일부 담당 해제 상태 없음; Q는 P 행 잠금을 기다리지 않음 |

RV-02의 ‘경계 이하’는 임의의 JSON이 성공한다는 뜻이 아니다. 실제 필드는 유효하고 JSON 공백으로 정확한 바이트 수를 맞춘 fixture를 사용한다. RV-03의 7개 전송 구간이 1초를 넘었거나 zone이 깨끗하지 않으면 제품 결함으로 판정하기 전에 환경을 초기화하고 재현한다.

보완 시나리오는 기존 ID의 더 구체적인 환경·실패 조건을 확장할 수 있다. 예를 들어 TK-15와 RV-11은 하나의 실행에서 함께 증명할 수 있지만 두 요구의 증거를 보고서에 연결한다.

## 7. 동시성 재현 설계

### 7.1 공통 장치

독립 HTTP 클라이언트·독립 DB 연결을 사용한다. 같은 Session을 여러 스레드에서 공유하지 않는다. 테스트 모듈의 계측 adapter로 ‘잠금 획득 전’, ‘잠금 획득 후’, ‘commit 전’ 이벤트를 관찰한다. 제어용 HTTP 엔드포인트를 제품에 추가하지 않는다.

barrier는 경쟁 요청이 시작할 위치에만 둔다. 같은 배타 잠금을 두 요청이 모두 획득한 뒤 만나게 하는 barrier는 테스트 자체를 교착시키므로 사용하지 않는다. 테스트 전체 상한은 일반 15초, 의도적 timeout 검증은 설정 시간 + 5초다. 계측 대기는 종료 이벤트 또는 timeout으로 반드시 풀리고 연결은 finally에서 정리한다.

### 7.2 같은 업무 동시 수정 — TK-09

1. T1 version=1을 준비하고 A/B가 각각 expected_version=1의 다른 변경을 보낸다.
2. A가 P의 잠금을 얻은 뒤 commit 직전에 멈춘다.
3. B가 같은 P 잠금을 기다리는 것을 별도 관리자 연결의 pg_stat_activity/pg_locks 또는 계측 이벤트로 확인한다.
4. A를 해제해 commit하고 B가 계속 진행하게 한다.
5. 응답은 A 200, B 409여야 한다. T1 version=2이고 A의 변경만 남아야 한다.
6. 다음 반복에서는 B를 먼저 잠그도록 바꾸어 승자가 고정된 코드가 아님을 확인한다.

대기 시간 자체의 우연한 순서에 기대지 않는다. 경합 대상 아닌 T5와 Q는 변경되지 않아야 한다.

### 7.3 팀원 제거와 담당 배정 — TK-12

**제거가 먼저:** A의 B 제거 요청이 잠금·담당 해제를 완료하고 commit → 후속 A의 업무 배정 요청이 최신 소속을 확인 → B 배정 422, T1 담당 null.

**배정이 먼저:** A의 B 배정 요청이 성공·commit → 후속 A의 B 제거가 담당 해제·업무 version 증가 → 최종 담당 null. 중간 200 응답은 가능하지만 최종 비소속 담당자는 없어야 한다.

각 순서에서 프로젝트·업무 version을 검사한다. 업무 내용·작성자 ID는 보존되어야 한다.

### 7.4 소유권 이전과 기존 소유자의 수정 — PM-15

이전 commit 후 이전 소유자의 대기 중 설정 요청은 잠금 이후 역할 검사에서 403이다. 설정 변경을 먼저 commit시켰다면 오래된 expected_version으로 보낸 이전 요청은 409다. 자동 버전 갱신 후 재시도하지 않는다. 모든 최종 상태에서 owner_user_id는 정확한 멤버십을 참조해야 한다.

### 7.5 삭제와 생성 — TK-13

삭제가 먼저 commit하면 후속 생성은 프로젝트 없음 404다. 생성이 먼저 commit하면 201 뒤 프로젝트 삭제로 업무가 함께 사라질 수 있다. 이 결과를 ‘생성 201이면 영구히 남아야 함’으로 오판하지 않는다. 어느 순서에서도 고아 업무가 남으면 실패다.

### 7.6 한 응답 snapshot — QF-07

읽기 전용 REPEATABLE READ의 첫 소속/목록 쿼리 뒤 count 또는 rows 직전에 다른 연결이 신규 업무를 commit한다. 해당 응답의 total과 items는 같은 이전 snapshot을 반영해야 한다. 다음 새 요청에서는 신규 업무가 보여야 한다. 조회 구현이 단일 SELECT이면 동일한 결과 보장을 증명하는 쿼리·통합 검증을 사용한다.

### 7.7 반복 정책

동시성 시나리오는 두 순서를 각각 독립 데이터로 10회씩 실행한다. 시연 후보에서는 각 순서를 30회씩 실행해 0건의 무결성 위반을 요구한다. 반복은 임의 timing 문제를 숨기는 retry가 아니다. 한 번이라도 무결성 실패하면 전체 실패다.

## 8. 실패 주입과 복구

| 주입 지점 | 방법 | 검증 |
|---|---|---|
| 프로젝트 insert 후 멤버십 insert 전 | 테스트 서비스/저장 계층에서 예외 발생 | 프로젝트 0행, 멤버십 0행 |
| 담당 해제 후 멤버십 delete 전 | DB/서비스 예외 발생 | 담당자·version·멤버십 원상태 |
| 프로젝트 자식 삭제 후 parent delete 전 | 예외 발생 | 자식과 부모 전부 유지 |
| DB commit 완료 후 HTTP 응답 전 | 테스트 프록시가 응답을 폐기 | DB 성공 유지, UI 결과 불명 안내, 자동 POST 없음 |
| 조회 응답 순서 역전 | Component의 지연 응답 큐 | 최신 query만 화면 반영 |
| pool 대기·행 잠금 | 제어 연결로 자원 점유 | 제한 시간·503·연결 반환 |
| DB 연결 실패 | disposable DB 중단 또는 테스트 연결 오류 | 준비 상태 503, 비밀값 미노출 |

실패 주입이 정상 경로와 다른 비즈니스 로직을 사용하지 않게 한다. rollback을 확인할 때는 같은 Session의 메모리 객체가 아니라 새로운 연결로 DB 최종 상태를 읽는다.

## 9. 브라우저·접근성 계획

### 9.1 E2E 여정

| ID | 여정 | 주요 증거 |
|---|---|---|
| E2E-01 | A 가입→P 생성→B 가입·추가→B 담당 업무→B 상태 변경→A 새로고침 | 실제 두 context, API·DB 결과 일치 |
| E2E-02 | C의 P·업무 URL 직접 접근과 API 호출 | 화면·응답 모두 접근 차단, 데이터 미노출 |
| E2E-03 | A/B 같은 업무 편집→A 저장→B 충돌→수동 재적용 | 초안 보존, 자동 overwrite 없음 |
| E2E-04 | A→B 소유권 이전→A 권한 제한→B가 A 제거 | 메뉴와 API 권한 일치, 담당 해제·업무 보존 |
| E2E-05 | 새로고침·API 재시작→데이터 확인→P 삭제 | 지속성 확인, Q/T5 보존 |

각 여정은 독립 fixture로 시작한다. 핵심 E2E는 기본적으로 API 모킹을 금지한다. 네트워크 장애 주입 시에는 실제 요청·commit을 유지하고 지정된 응답 전달만 제어한다.

### 9.2 수동·자동 화면 체크

- 360×800, 768×1024, 1440×900에서 모든 주요 화면 확인.
- 로그인·등록·프로젝트 생성·업무 폼의 레이블·오류 연결과 첫 오류 포커스 확인.
- Tab/Shift+Tab, Enter, Space, Esc로 핵심 여정 수행.
- Modal 열기·닫기·저장 실패·삭제 취소 후 포커스 위치 확인.
- 200% 확대, 긴 한글 제목, 최대 길이 설명, 팀원 이름 중복 확인.
- 상태·지연 의미가 색만으로 전달되지 않는지 확인.
- 일반 텍스트 대비 4.5:1 기준 확인; 보드 가로 스크롤은 영역 안에서만 발생.
- 자동 접근성 검사에서 critical/serious 0개, 수동 키보드 핵심 흐름 실패 0개.

자동 접근성 검사 통과만으로 보조기술 전체 호환을 주장하지 않는다. 실제 사용한 OS·브라우저·검사 도구와 수동 확인 범위를 기록한다.

## 10. 성능 계획

### 10.1 공통 조건

기준 환경은 4 vCPU/8GB, 단일 API worker, PostgreSQL 18, pool 5+5다. 다른 환경에서 측정하면 별도 결과로 표시한다. 성능 도구는 Locust를 테스트 전용 의존성으로 계획한다. 기존 프로젝트 런타임에 부하 도구를 배포하지 않는다.

10초 워밍업 후 60초 측정, 동시 가상 사용자 10명, 사용자당 요청 후 250ms 대기를 기본으로 한다. 로그인은 측정 전에 완료한다. API 지연은 DB 처리와 직렬화를 포함한 프록시 응답 완료까지 측정한다. 네트워크 위치와 TLS 사용 여부를 보고서에 기록한다.

각 측정 시나리오에서 최소 200개의 유효 요청 표본을 확보한다. 미달이면 시간을 늘리거나 ‘표본 부족’으로 남긴다. 성공 응답만 골라 p95를 낮추지 않고 모든 완료 응답의 지연·status 분포와 성공 요청 분포를 함께 기록한다.

| ID | 데이터·작업 | 판단 |
|---|---|---|
| PERF-01 | P 업무 1,000개, 설명 평균 200자, 목록 page_size=20, 필터·검색 조합 | GET p95 ≤500ms 목표, 예상 밖 5xx=0 |
| PERF-02 | 사용자별 별도 프로젝트, 업무 1,000개씩, 충돌 없는 업무 수정 | 쓰기 p95 ≤1초 목표, 예상 밖 5xx=0 |
| PERF-03 | 10명이 같은 프로젝트·업무를 변경 | 200/409/503 비율·잠금 시간을 분리 보고; 무결성 위반·고아·lost update 0 |
| PERF-04 | page_size=100, 각 업무 설명 10,000자; 대량 해제·삭제도 별도 실행 | 응답 바이트·메모리·쿼리 수·rollback·복구 시간 기록; PERF-01과 같은 SLO라고 주장하지 않음 |

성능 목표 미달은 원인·재현 조건을 남긴다. 기능 무결성은 타협하지 않는다. 단순히 캐시·워커 수를 늘리기 전에 query plan·N+1·응답 크기·pool 대기·행 잠금 중 어느 비용인지 분리한다. 지원 규모나 목표 변경은 사용자 검토 후 명세에 반영한다.

## 11. 테스트 파일·명령 계약

### 11.1 추가 파일의 계획 위치

| 계획 파일 | 책임 |
|---|---|
| backend/tests/conftest.py | 테스트 환경 guard·DB 수명·Clock·인증 fixture |
| backend/tests/unit/test_domain_rules.py | 날짜·상태·권한 predicate |
| backend/tests/integration/test_constraints.py | 복합 FK·지연 FK·실제 commit |
| backend/tests/integration/test_concurrency.py | 실행 순서·독립 연결·잠금 |
| backend/tests/integration/test_contract_review.py | RV-02·05·07·10·15·18의 앱/DB 부분 |
| backend/tests/system/test_proxy_contracts.py | RV-01·03·04·13·14·16 |
| backend/tests/system/test_migrations_seed.py | RV-06·12 |
| frontend/tests/component/review-cases.spec.ts | RV-08·09·11의 UI 부분 |
| frontend/tests/e2e/core-journeys.spec.ts | E2E-01~05 |
| frontend/tests/e2e/browser-input.spec.ts | RV-17 |
| tests/performance/locustfile.py | PERF-01~04 |
| compose.test.yaml | 독립 DB·API·프록시·프론트의 테스트 구성 |

이 목록과 5절의 기본 파일 배치는 제품 구현 계획에서 구체적인 fixture·함수 인터페이스로 이어진다. 지금 테스트 파일을 빈 껍데기로 만들지 않는다.

### 11.2 구현 후 제공할 명령

아래는 향후 실행 인터페이스다. 현재 저장소에서 실행하면 성공해야 하는 명령 목록이 아니다.

| 작업 위치 | 명령 | 결과 |
|---|---|---|
| backend | `uv sync --frozen` | lock 기반 의존성 설치 |
| backend | `uv run pytest tests/unit -q` | 단위 테스트 결과 |
| backend | `uv run pytest tests/integration -q --junitxml=reports/integration.xml` | API·DB 테스트 결과 |
| backend | `uv run pytest tests/system -q --junitxml=reports/system.xml` | 프록시·환경 결과 |
| backend | `uv run ruff check .` 및 `uv run mypy app` | 정적·타입 결과 |
| frontend | `npm ci` | lock 기반 프론트 의존성 설치 |
| frontend | `npm run test:unit -- --run` | Vitest 결과 |
| frontend | `npm run typecheck` 및 `npm run lint` | TypeScript·lint 결과 |
| frontend | `npm run build` | 배포용 빌드 |
| frontend | `npm run test:e2e` | Playwright 보고서 |

pytest marker는 `p0`, `p1`, `concurrency`, `system`을 strict markers로 등록한다. 빠른 PR 실행에서도 변경 기능의 P0·통합 테스트를 빠뜨리지 않는다. 최종 후보에서는 전체 86개 ID와 E2E·환경 검증을 실행한다.

## 12. CI·재실행 정책

1. 정적 검사·단위·컴포넌트를 먼저 실행한다.
2. G0를 통과한 격리 환경에서 API/DB 통합을 실행한다.
3. 성공한 빌드로 핵심 E2E와 별도 프록시 검증을 실행한다.
4. 최종 후보 커밋에서 동시성 반복·브라우저 행렬·성능을 실행한다.
5. 결과물에 같은 커밋 SHA·lockfile hash·DB 버전·앱 설정 프로필을 연결한다.

기본 재시도는 0이다. 환경성 E2E 실패를 조사할 때 1회 재시도를 켤 수 있지만 첫 실패 후 통과는 flaky로 분리하고 MVP 통과로 세지 않는다. Playwright도 처음부터 통과한 테스트와 retry 후 통과한 테스트를 구분한다. [Playwright 재시도 문서](https://playwright.dev/docs/test-retries)

실패를 지우고 마지막 성공만 보고하지 않는다. 원인과 수정이 확인된 뒤 같은 대상에 재검증한다. 수정되지 않은 테스트를 skip·xfail로 바꿔 gate를 통과시키지 않는다. 테스트 대상 기능 자체가 범위에서 제외될 때만 사용자 검토를 거쳐 ID·범위를 수정한다.

## 13. 결과 기록·완료 기준

### 13.1 실행 기록 형식

각 결과는 ID, 대상 커밋, 환경, fixture 버전, 실행 명령, 실제 결과, 기대 결과, 상태, 증거 파일, 관련 결함을 가진다. 상태는 NOT_RUN, PASS, FAIL, BLOCKED, FLAKY 중 하나다. 현재 제품 시나리오의 상태는 모두 NOT_RUN이며 환경 착수 조건은 미충족이다.

산출물은 JUnit XML, Playwright HTML/trace, 실패 스크린샷, 익명화 로그, 성능 CSV/요약, DB invariant 검사 결과다. 보고서는 실제 개인 계정·쿠키·비밀번호를 담지 않고 저장소에 비밀값을 커밋하지 않는다. 실행 로그와 테스트용 자격증명이 들어갈 수 있는 trace는 제한된 로컬/CI 산출물로 관리한다.

### 13.2 최종 통과 기준

- [ ] 86개 기능 검증 ID 각각에 하나 이상의 실행 증거가 연결되어 있다.
- [ ] P0·P1의 FAIL/BLOCKED/FLAKY가 0이다.
- [ ] E2E-01~05가 실제 연결 환경에서 통과했다.
- [ ] DB 제약·원자성·경합의 두 실행 순서가 확인되었다.
- [ ] OpenAPI 21개 엔드포인트와 명세의 차이가 없다.
- [ ] 지원 브라우저·반응형·키보드 기준을 통과했다.
- [ ] 배포 프로필의 쿠키·Origin·프록시·문서 비공개 정책을 확인했다.
- [ ] 성능 목표 충족 또는 사용자 검토를 거친 지원 범위 조정이 기록되어 있다.
- [ ] 알려진 제약과 재현 절차를 README/검토 문서에 남겼다.

커버리지 비율 하나가 이 목록을 대체하지 않는다. 라인 커버리지는 빠진 경로를 찾는 보조 지표로 보고하고, 권한·트랜잭션·실패 분기의 실제 결과를 우선한다.

## 14. 계획 자체의 검증

이번 문서 작성 단계에서는 기존 68개 ID, RV 18개, E2E 5개, PERF 4개가 중복·누락 없이 정의되고 상위 요구사항·제약과 연결되는지 확인한다. 마크다운 링크·예시 데이터·정책 값을 대조한다. 이것은 제품 테스트 실행 결과가 아니며, 구현 착수 전에 계획을 검토할 수 있게 하는 문서 품질 확인이다.
