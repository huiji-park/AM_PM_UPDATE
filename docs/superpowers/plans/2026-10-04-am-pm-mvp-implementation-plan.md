# AM_PM MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 가입·인증·프로젝트·팀원·업무 관리를 실제 DB와 연결하여 학습·포트폴리오용 MVP를 완성한다.

**Architecture:** 기존 저장소에 FastAPI 모듈형 모놀리스와 Vue SPA를 새로 추가한다. 서비스가 트랜잭션을 소유하고 PostgreSQL 제약과 프로젝트 행 잠금으로 무결성을 지킨다. 브라우저는 동일 출처 프록시를 통해 쿠키 세션을 사용한다.

**Tech Stack:** Python 3.13, FastAPI, Pydantic 2, SQLAlchemy 2 동기 방식, psycopg 3, Alembic, PostgreSQL 18, Vue 3, TypeScript strict, Vite, Pinia, Vue Router, Node.js 24 LTS, uv, npm, pytest, Vitest, Playwright.

**Spec:** [상세 설계 0.2](../specs/2026-10-03-am-pm-mvp-design.md), [API 계약](../specs/2026-10-03-am-pm-mvp-api-contract.md), [화면 명세](../specs/2026-10-03-am-pm-mvp-screens-verification.md), [제약 검토](../specs/2026-10-03-am-pm-mvp-constraints-review.md), [테스트 계획](2026-10-03-am-pm-mvp-test-plan.md).

작성일: 2026-10-04, Asia/Seoul. 상태: 사용자 검토용 실행 계획. 모든 작업은 미착수이며 제품 테스트 결과는 NOT_RUN이다. 파일 경로·명령·함수는 앞으로 만들 계약이다. [환경 설정 계획](2026-10-04-am-pm-mvp-environment-plan.md)과 함께 사용한다.

## Global Constraints

- 원본 `Client/`, `Server/`는 비교용으로 보존하고 새 코드는 `frontend/`, `backend/`, `infra/`에 둔다.
- Python 3.13, PostgreSQL 18, SQLAlchemy 2 + psycopg 3 동기 방식, API 기본 worker 1개다.
- 로컬 브라우저 출처는 `http://localhost:5173`, API base는 `/api/v1`이다.
- 세션은 32바이트 난수 토큰, DB에는 SHA-256 해시만 저장, 절대 수명 604800초, sliding 갱신 없음이다.
- 개발 쿠키 `ampm_session`, production 쿠키 `__Host-ampm_session`; HttpOnly, SameSite=Lax, Path=/, Domain 없음이다.
- production은 HTTPS와 Secure=true를 강제하고 `/api/docs`, `/api/openapi.json`을 비공개로 한다.
- POST/PATCH/DELETE는 정확한 Origin과 `X-AMPM-Request: 1`을 요구한다. body가 있으면 JSON만 허용한다.
- 요청 본문 상한은 262144바이트, Content-Encoding은 없음 또는 identity만 허용한다.
- 비밀번호는 12~128 Unicode 코드 포인트, trim·정규화하지 않는다.
- 프로젝트 이름 1~100, 설명 최대 5000; 업무 제목 1~200, 설명 최대 10000; 검색어 최대 100이다.
- 날짜는 YYYY-MM-DD, 시각은 UTC의 Z 표현, 지연 판단 시간대는 Asia/Seoul이다.
- 업무 상태는 TODO/IN_PROGRESS/DONE; DONE일 때만 completed_at이 존재한다.
- 모든 프로젝트 쓰기는 프로젝트 행 FOR UPDATE 후 최신 소속·권한·버전을 검사한다.
- 인증 Session은 user UUID만 반환하고 닫는다. 서비스는 별도 Session과 명시적 transaction을 사용한다.
- pool_size=5, max_overflow=5, pool_timeout=3초, lock_timeout=3초, statement_timeout=5초다.
- expected_version은 bool을 거부하는 양의 정수다. 권한 확인이 버전 정보 공개보다 먼저다.
- 페이지 기본 page=1/page_size=20, 최대 page_size=100; total=0이면 total_pages=0이다.
- 프론트 요청 제한 시간 15초, 검색 debounce 300ms, 쓰기 자동 재시도 없음이다.
- 인증 상태는 unknown/authenticated/anonymous/unavailable; 토큰을 브라우저 저장소에 넣지 않는다.
- 기존 데이터 이관, 이메일 인증·복구, 채팅, 파일, 간트, 실시간 동기화, 드래그 보드는 범위 밖이다.

## Review Focus

1. 계정 전환 뒤 이전 사용자의 늦은 GET이 도착하는 경우: 인증 세대가 다르면 데이터·초안을 폐기한다. I04/RV-09.
2. 멤버 제거·담당 배정·소유권 이전이 경합하는 경우: 잠금 후 최신 권한을 확인하고 전부 commit 또는 rollback한다. I06·I08/RV-18.
3. 생성 commit 후 응답만 유실되는 경우: 중복 POST를 보내지 않고 결과 확인을 안내한다. I10·I12/RV-11.
4. 다른 시점의 보드 응답이 같은 업무를 반환하는 경우: ID별 최대 version 한 건만 실제 status 열에 표시한다. I10/RV-08.
5. 테스트 DB 주소·이름·실행 토큰 중 하나가 잘못된 경우: DDL·정리 전에 중단한다. I01/RV-06.

## 1. 진행 방식과 의존 관계

환경 E01~E02·E03의 Docker engine 준비 → I01 → I02 → I03 → I04 → I05 → I06 → I07 → I08 → I09 → I10 → I11 → I12 → I13 순서로 진행한다. E03의 DB 생성·검증은 I01의 Compose 생성 후 수행한다. 환경 E04는 I01·I04의 manifest 생성 시, E05는 I02 이후, E06은 I11 이후에 적용한다. E01~E06은 환경 계획의 작업 번호다.

| 구간 | 사용자에게 보여 줄 결과 | 종료 기준 |
|---|---|---|
| I01~I02 실행·DB | health와 빈 DB 마이그레이션 | 테스트 DB 격리, 제약의 실제 commit 검증 |
| I03~I04 인증 | 가입→로그인→새로고침→로그아웃 | API·화면·세션 수명 통과 |
| I05~I07 협업 | 프로젝트 생성·설정·팀원·소유권 | 타 프로젝트 격리, 실패 rollback, 화면 권한 일치 |
| I08~I10 업무 | 업무 생성·필터·보드·충돌 복구 | 경쟁 수정, 날짜·상태, 화면 갱신 통과 |
| I11~I13 시연 후보 | 실제 프록시 배포 구성과 재현 가능한 시연 | 전체 86 ID, E2E 5개, 성능 4개 증거 |

달력 일정은 개발자가 직접 학습·구현하는 시간에 따라 달라진다. 각 구간의 검증 결과로 다음 범위를 확정하며, 통과하지 않은 작업을 완료로 넘기지 않는다. 외부 공개 배포는 이 계획의 자동 실행 대상이 아니다.

각 작업은 표시된 파일에 한정해 stage하고 의미 있는 커밋을 남긴다. 다른 변경이 있으면 `git add .`를 사용하지 않는다. 구현 시작 시 worktree 절차로 격리하고, 이 문서·명세를 해당 체크아웃에 포함한다. 커밋 메시지는 아래 작업 끝의 제안을 사용한다.

## 2. 파일·타입 경계

| 위치 | 책임 |
|---|---|
| backend/app/main.py | create_app·lifespan·라우터 조립 |
| backend/app/core/ | 설정, 시계, DB, 오류, 요청 보호, 로깅 |
| backend/app/auth/ | 인증 모델·스키마·서비스·라우터·dependency |
| backend/app/projects/ | 프로젝트·멤버십 모델, 스키마, 잠금·권한, 서비스·조회·라우터 |
| backend/app/tasks/ | 업무 모델, 날짜·완료 규칙, 스키마, 서비스·조회·라우터 |
| backend/app/cli/ | 개발 시드·만료 세션 정리 |
| backend/migrations/ | DB revision, 명명된 FK·CHECK·인덱스 |
| frontend/src/lib/ | fetch·오류·페이지 타입·조회 취소 |
| frontend/src/features/auth, projects, tasks/ | 기능별 API·타입·페이지·컴포넌트 |
| frontend/src/components/, styles/ | 접근 가능한 공통 폼·Modal·상태·레이아웃 |
| infra/nginx/, compose*.yaml | 동일 출처 프록시와 환경별 연결·노출 범위 |
| scripts/, .github/workflows/ | 테스트 환경 수명·검증·CI |

Python `SessionFactory`는 `sessionmaker[Session]`, `Clock`은 `Callable[[], datetime]` 타입 별칭이다. `core/db.py`의 `Database`는 `engine: Engine`, `sessions: SessionFactory`, `read_sessions: SessionFactory`, `dispose() -> None`을 제공한다. read_sessions는 첫 SQL 전에 REPEATABLE READ·READ ONLY를 설정한다. 서비스 생성자는 공통으로 `(db: Database, clock: Clock)`을 받는다. 라우터에서 DTO를 검증하고 서비스에서 transaction을 열며, 세션 종료 전에 응답 DTO를 구성하고 commit 성공 후 반환한다. ORM 객체를 닫힌 세션 밖에 넘기지 않는다.

DTO는 API 계약의 필드명을 그대로 사용한다. `User`, `UserSummary`는 auth/schemas.py; `ProjectCreate`, `ProjectPatch`, `ProjectSummary`, `ProjectDetail`, `Member`, `MemberAdd`, `MemberAdded`, `Transfer`는 projects/schemas.py; `TaskCreate`, `TaskPatch`, `Task`, `TaskQuery`는 tasks/schemas.py에 둔다. `VersionCommand(expected_version: StrictInt>0)`, `PageQuery`, `Page[T]`는 core/schemas.py에 둔다. MemberAdded는 `member: Member`, `project_version: int`다. 입력 모델 extra=forbid, PATCH는 model_fields_set으로 누락과 null을 구분한다.

TypeScript의 동일 DTO 이름은 대응 기능의 types.ts에 둔다. 원문 JSON을 타입 단언만으로 신뢰하지 않고 fetch 래퍼에서 HTTP·공통 오류를 확인한다. 이 MVP에서 SDK 생성기를 별도 도입하지 않는다. OpenAPI와 응답 fixture를 계약 테스트로 비교하여 수동 타입의 불일치를 잡는다.

## 3. 작업별 실행 계획

### I01. 실행 기반·health·테스트 DB 안전장치

**Files:** Create `backend/pyproject.toml`, `backend/uv.lock`, `backend/.python-version`, `backend/.env.example`, `backend/app/__init__.py`, `backend/app/main.py`, `backend/app/core/{config,db,clock,errors,schemas,logging}.py`, `backend/app/health.py`, `backend/tests/conftest.py`, `backend/tests/support/database.py`, `backend/tests/unit/test_settings.py`, `backend/tests/system/test_database_guard.py`, `backend/tests/integration/test_health.py`, `compose.yaml`, `compose.test.yaml`, `.gitattributes`, `infra/.env.example`, `infra/postgres/bootstrap.sql`, `infra/postgres/bootstrap.sh`, `docs/runbooks/local-development.md`; Modify `.gitignore`, `README.md`. 각 Python 패키지에 __init__.py를 추가한다.

**Interfaces:** Produces `Settings`, `create_app(settings: Settings | None = None) -> FastAPI`, `create_database(settings: Settings) -> Database`, `utc_now() -> datetime`, `AppError(status: int, code: str, message: str, details: list[dict], context: dict)`. 테스트 지원 `validate_test_target(name: str, host: str, app_env: str, run_token: str, manifest: dict) -> None`, `provision_test_database(run_id: str, worker_id: str) -> str`는 DB URL을 반환하되 출력하지 않는다. pytest fixture `client`, `db`, `clock`, `unsafe_headers`를 제공한다.

- [ ] 테스트 작성: `test_guard_rejects_each_mismatch_before_ddl`에서 이름·host·APP_ENV·token을 각각 바꾼 4개 입력 모두 `pytest.raises(ValueError)`와 `assert ddl_calls == []`; `test_live_survives_database_failure`에서 `assert live.status_code == 200`, `assert ready.status_code == 503`을 확인한다. RV-06·13.
- [ ] manifest와 테스트 도구를 먼저 마련한 뒤 backend에서 `uv run pytest tests/unit/test_settings.py tests/system/test_database_guard.py tests/integration/test_health.py -q` 실행. 구현 전 기대: import 또는 미구현 health/guard assertion FAIL. 도구 설치 실패는 RED가 아니라 환경 BLOCKED다.
- [ ] Settings의 production 검증, DB pool·timeout, 오류 envelope·request_id·redaction을 구현한다. readiness는 SELECT 1과 migration head를 검사하고 I02 전에는 미준비 503이다. 테스트 provisioner만 CREATE/DROP DATABASE 권한을 가지며 manifest exact match로 정리한다. base Compose는 개발 DB만, test Compose는 별도 DB 서비스로 시작한다.
- [ ] 같은 명령 PASS, `uv run ruff check .`, `uv run mypy app` PASS. 읽기 전용 검증으로 잘못된 대상 DB에 접속·DDL이 없음을 확인한다. pytest p0/p1/concurrency/system marker를 strict로 등록한다.
- [ ] 위 Files만 검토·stage하고 `chore: establish API runtime and isolated test databases`로 커밋한다.

### I02. 도메인 모델·초기 마이그레이션

**Files:** Create `backend/app/auth/models.py`, `backend/app/projects/models.py`, `backend/app/tasks/models.py`, `backend/app/models.py`, `backend/alembic.ini`, `backend/migrations/env.py`, `backend/migrations/script.py.mako`, `backend/migrations/versions/0001_initial.py`, `backend/tests/integration/test_constraints.py`, `backend/tests/system/test_migrations_seed.py`; Modify `backend/app/health.py`, `backend/tests/conftest.py`.

**Interfaces:** Consumes I01 `Database`. Produces SQLAlchemy `Base` and `UserRow`, `AuthSessionRow`, `ProjectRow`, `MembershipRow`, `TaskRow` with spec field names; `app/models.py` exports Base and imports every model for Alembic metadata. Expected revision constant `0001_initial` is the app's readiness target until a new migration is added.

- [ ] `test_owner_must_be_member_at_commit`: owner membership 없음으로 commit하면 `pytest.raises(IntegrityError)`; 같은 transaction에서 둘을 만들면 `assert project.owner_user_id == membership.user_id`. `test_assignee_requires_same_project_membership`: 다른 프로젝트 담당자 commit 거부; assignee null은 허용. PM-03·TK-03.
- [ ] backend `uv run pytest tests/integration/test_constraints.py -q` 실행, 기대: 누락된 모델/제약 FAIL.
- [ ] UUIDv4·UTC timestamps·이메일 unique·버전 양수·상태·기간·완료 CHECK 및 인덱스를 작성한다. projects→memberships 복합 FK는 memberships 생성 후 추가하고 DEFERRABLE INITIALLY DEFERRED/NO ACTION, task 담당 FK는 MATCH SIMPLE/NO ACTION으로 정의한다. migration downgrade는 순환 FK부터 제거한다. 앱 startup create_all은 사용하지 않는다.
- [ ] 독립 DB에서 `uv run alembic upgrade head`, 제약 테스트 PASS; 별도 빈 disposable DB에서 downgrade 한 단계→upgrade 후 `test_migration_round_trip_restores_constraints` PASS. ready 200 확인. 테스트 명령은 fixture가 선택한 URL만 사용한다.
- [ ] `feat: define PostgreSQL domain constraints and initial migration`으로 커밋한다.

### I03. 인증 API·요청 보호

**Files:** Create `backend/app/auth/{schemas,service,router,dependencies,passwords,sessions}.py`, `backend/app/core/request_guard.py`, `backend/tests/integration/test_auth.py`, `backend/tests/integration/test_contract_review.py`; Modify `backend/app/main.py`, `backend/app/core/errors.py`, `backend/tests/conftest.py`.

**Interfaces:** Consumes I01 Database/Clock, I02 rows. Produces `AuthService.register(data: Register) -> User`, `.login(data: Login, current_token: str | None) -> LoginResult`, `.logout(token: str | None) -> None`, `.resolve_user_id(token: str | None) -> UUID`, `.get_user(user_id: UUID) -> User`; LoginResult 내부 dataclass는 user/token/expires_at을 가지며 HTTP 응답 DTO가 아니다. `require_user_id(request: Request) -> UUID`는 resolve_user_id 호출 후 닫힌 인증 Session만 사용한다. `RequestGuardMiddleware`는 순수 ASGI receive를 감싸 streaming body 상한을 적용한다.

- [ ] `test_register_does_not_login`에서 `assert status == 201`, `assert 'set-cookie' not in headers`, hash가 원문과 다름; `test_expiry_boundary`에서 만료 전200/시각401; `test_streamed_body_limit`에서 262144 이하는 스키마 처리, 262145는413; `test_auth_then_write_uses_distinct_sessions`에서 Session identity가 다르고 누수0을 확인한다.
- [ ] `uv run pytest tests/integration/test_auth.py tests/integration/test_contract_review.py -q` 실행, 새 시나리오 기대 FAIL.
- [ ] A01~A04, Argon2, 이메일 정규화·동시 중복409, 로그인 동일401, 현재 쿠키의 이전 세션 교체, 7일 절대 만료, idempotent logout을 구현한다. 모든 unsafe 요청의 Origin/header·JSON/encoding을 검사하고 개발 Swagger가 전용 헤더를 전송하도록 OpenAPI 또는 interceptor를 구성한다. Pydantic 오류에서 input/context 원문을 제거하고 허용한 field/message만 반환한다. 보호·auth 응답 no-store, 비밀 없는 로그, 세션 풀 대기503도 적용한다.
- [ ] AU-01~12·15, RV-02·05·07·14의 API 부분 PASS. 비밀번호 11/12/128/129 코드 포인트·이모지·공백, 현재 브라우저/다른 브라우저 세션, DB raw token 미저장을 검사한다. 위 두 파일과 I01~I02 회귀 PASS.
- [ ] `feat: implement cookie sessions and unsafe request guards`로 커밋한다.

### I04. 인증 화면·공통 HTTP·라우팅

**Files:** Create `frontend/package.json`, `frontend/package-lock.json`, `frontend/.node-version`, `frontend/index.html`, `frontend/tsconfig.json`, `frontend/tsconfig.node.json`, `frontend/vite.config.ts`, `frontend/eslint.config.js`, `frontend/vitest.config.ts`, `frontend/src/{main.ts,App.vue,vite-env.d.ts}`, `frontend/src/router/index.ts`, `frontend/src/lib/{http,errors,types}.ts`, `frontend/src/features/auth/{api,types,store}.ts`, `frontend/src/features/auth/{LoginPage,RegisterPage}.vue`, `frontend/src/components/{AppShell,FormField,ErrorNotice,LoadingState,EmptyState,ConfirmDialog}.vue`, `frontend/src/styles/{tokens,base}.css`, `frontend/tests/component/{auth-bootstrap,query-and-errors,review-cases}.spec.ts`, `frontend/tests/setup.ts`.

**Interfaces:** Produces `apiRequest<T>(path: string, options?: RequestOptions): Promise<T>`; RequestOptions는 method/body/signal을 가진다. `ApiError`는 status/code/message/details/context/requestId를 가진다. 204는 undefined로 반환한다. `useAuthStore()`는 status/user/generation, `bootstrap(): Promise<void>`, `login(input: Login): Promise<void>`, `logout(): Promise<void>`, `invalidate(): void`를 제공한다. `safeNext(raw: unknown): string`은 허용된 내부 경로만 반환한다.

- [ ] `late_response_from_previous_account_is_discarded`: A GET 지연→logout→B login→A 응답에서 `expect(renderedUser).toBe('B')`, `expect(protectedCache).not.toContain(AData)`. `bootstrap_network_failure_is_unavailable`, `unsafe_next_falls_back`, `proxy_html_error_is_readable`도 추가한다. AU-13, QF-13·18~20, RV-09.
- [ ] `npm run test:unit -- --run` 실행, 기대: 미구현 store/래퍼 assertion FAIL. Node/의존성 문제는 환경 실패로 분리한다.
- [ ] Vite proxy `/api`→localhost:8000, credentials=same-origin, unsafe 헤더, 15초 timeout, 비JSON 오류 변환을 구현한다. 브라우저 Origin을 코드에서 설정하지 않는다. auth 세대 변경 시 보호 GET·초안 폐기, 네트워크 오류는 unavailable, 입력 길이는 Array.from(value).length를 사용한다. S01/S02/S09와 보호 라우트 bootstrap을 만든다. Modal 포커스 트랩·복귀·Esc, 폼 label/error 연결을 이 단계부터 적용한다.
- [ ] 컴포넌트 PASS, `npm run typecheck`, `npm run lint`, `npm run build` PASS. AU-04의 프론트 경계와 모든 auth 상태를 확인한다. 각 상태를 제품에 숨은 테스트 페이지로 추가하지 않는다.
- [ ] `feat: add authentication screens and request lifecycle`로 커밋한다.

### I05. 프로젝트 API·조회 snapshot

**Files:** Create `backend/app/projects/{schemas,access,service,queries,router}.py`, `backend/app/core/query.py`, `backend/tests/integration/test_projects_members.py`, `backend/tests/integration/test_query_contracts.py`; Modify `backend/app/main.py`, `backend/tests/conftest.py`.

**Interfaces:** Produces `lock_project(session: Session, project_id: UUID, actor_id: UUID, require_owner: bool = False) -> ProjectRow`; `ProjectService.create(actor_id: UUID, data: ProjectCreate) -> ProjectDetail`, `.get(actor_id: UUID, project_id: UUID) -> ProjectDetail`, `.list(actor_id: UUID, query: ProjectQuery) -> Page[ProjectSummary]`, `.update(actor_id: UUID, project_id: UUID, data: ProjectPatch) -> ProjectDetail`, `.delete(actor_id: UUID, project_id: UUID, expected_version: int) -> None`. ProjectQuery는 projects/schemas.py에 정의하며 공통 PageQuery + q/sort다.

- [ ] `test_creation_rollback_has_no_orphan`에서 실패 주입 후 `assert project_count == membership_count == 0`; `test_nonmember_cannot_observe_version`에서 `assert status == 404`; `test_list_count_and_rows_share_snapshot`에서 별도 commit 전 snapshot의 total/items가 일치함을 검증한다.
- [ ] `uv run pytest tests/integration/test_projects_members.py tests/integration/test_query_contracts.py -q` 실행, 신규 계약 기대 FAIL.
- [ ] P01~P05와 프로젝트 생성+소유자 멤버십 원자성, 이름·기간·버전·소유 권한을 구현한다. read_sessions로 소속/count/rows·집계를 같은 snapshot에서 읽고 ID tie-break, LIKE 리터럴 escape, 미정렬 UUID 안정 정렬을 적용한다. 삭제는 tasks→memberships→project 순서로 같은 transaction에서 수행한다. 상세 집계는 필터와 무관한 전체 업무 기준 floor(done/total*100)이다.
- [ ] PM-01~06·16~17, QF-01~03·06~07, RV-10 PASS. 보호된 읽기 snapshot이 시작된 뒤 제거된 멤버의 진행 중 응답은 허용하고 다음 새 요청404를 검사한다. 기존 auth 회귀 PASS.
- [ ] `feat: add transactional project management and snapshot reads`로 커밋한다.

### I06. 팀원·소유권 API와 경합

**Files:** Create `backend/app/projects/members.py`, `backend/tests/integration/test_concurrency.py`; Modify `backend/app/projects/{schemas,router,access}.py`, `backend/tests/integration/test_projects_members.py`, `backend/tests/integration/test_contract_review.py`, `backend/tests/conftest.py`.

**Interfaces:** Consumes lock_project, Database, Clock. Produces `MemberService.list(actor_id: UUID, project_id: UUID, query: PageQuery) -> Page[Member]`, `.add(actor_id: UUID, project_id: UUID, data: MemberAdd) -> MemberAdded`, `.remove(actor_id: UUID, project_id: UUID, member_id: UUID, expected_version: int) -> None`, `.leave(actor_id: UUID, project_id: UUID, expected_version: int) -> None`, `.transfer(actor_id: UUID, project_id: UUID, data: Transfer) -> ProjectDetail`.

- [ ] `test_removal_rolls_back_assignments_and_versions`에서 주입 오류 후 `assert assignee == B`, `assert task_version == 1`, 멤버십 유지. `test_waiting_old_owner_loses_privilege_after_transfer`는 transfer commit 후 대기 중 설정 변경 `assert status == 403`. PM-12·15/RV-18.
- [ ] `uv run pytest tests/integration/test_projects_members.py tests/integration/test_concurrency.py -q` 실행, 신규 시나리오 기대 FAIL.
- [ ] M01~M05를 구현한다. 정확한 등록 이메일만 조회하고 목록·autocomplete API는 만들지 않는다. 모든 쓰기 잠금 후 최신 role→version→대상 유효성 순서를 지킨다. 담당 해제는 영향받은 각 task.version/updated_at과 project.version을 올린다. creator·업무 내용·상태는 보존한다. 소유자 이탈 거부, 이전자는 일반 멤버로 유지한다.
- [ ] PM-07~15·18, RV-18의 멤버 경로 PASS. 독립 연결·이벤트로 두 commit 순서를 각 10회 재현하고 다른 프로젝트 Q의 쓰기가 P 잠금을 기다리지 않음을 검증한다. 동시성 test hook은 테스트 adapter이며 제품 HTTP에 노출하지 않는다.
- [ ] `feat: manage membership and ownership under project locks`로 커밋한다.

### I07. 프로젝트·팀원 화면

**Files:** Create `frontend/src/features/projects/{api,types,useProject}.ts`, `frontend/src/features/projects/{ProjectListPage,ProjectCreatePage,ProjectSettingsPage,MembersPage,ProjectLayout,MemberPicker}.vue`, `frontend/tests/component/projects.spec.ts`; Modify `frontend/src/router/index.ts`, `frontend/tests/component/query-and-errors.spec.ts`.

**Interfaces:** Produces projects api의 `listProjects(query: ProjectQuery): Promise<Page<ProjectSummary>>`, `getProject(id: string): Promise<ProjectDetail>`, `createProject(input: ProjectCreate): Promise<ProjectDetail>`, `updateProject(id: string, input: ProjectPatch): Promise<ProjectDetail>`, `deleteProject(id: string, expectedVersion: number): Promise<void>`, `listMembers(id: string, query: PageQuery): Promise<Page<Member>>`, `addMember(id: string, input: MemberAdd): Promise<MemberAdded>`, `removeMember(id: string, userId: string, expectedVersion: number): Promise<void>`, `leaveProject(id: string, expectedVersion: number): Promise<void>`, `transferOwnership(id: string, input: Transfer): Promise<ProjectDetail>`. `useProject(id: Ref<string>)`는 detail/loading/error/refetch를 제공한다. MemberPicker props는 projectId:string/modelValue:string|null/selectedUser:UserSummary|null, emit update:modelValue는 string|null이다.

- [ ] `member_picker_loads_page_two`에서 101번째 팀원 선택 가능; `deleting_last_row_moves_to_valid_page_once`에서 유효 페이지 한 번 이동; `member_cannot_open_owner_controls`에서 소유자 메뉴 없음·직접 설정 URL 접근 안내. QF-12·15.
- [ ] `npm run test:unit -- --run tests/component/projects.spec.ts tests/component/query-and-errors.spec.ts` 실행, 신규 화면 기대 FAIL.
- [ ] S03/S04/S07/S08·프로젝트 탭을 만들고 URL query를 조회 기준으로 사용한다. 제거·탈퇴·이전의 영향과 확인 대상을 명시하고 성공 뒤 상세·멤버·표시 업무를 무효화한다. 409는 초안을 보존해 최신 버전 비교를 제공한다. 소유권 이전 후 새 my_role을 반영한다.
- [ ] 컴포넌트·typecheck·lint·build PASS. 화면상 숨김과 서버403/404를 별개로 검증한다. 긴 제목·중복 표시 이름에서도 user ID로 선택을 유지한다.
- [ ] `feat: add project and membership workflows`로 커밋한다.

### I08. 업무 쓰기 API·도메인 규칙

**Files:** Create `backend/app/tasks/{schemas,rules,service,router}.py`, `backend/tests/unit/test_domain_rules.py`, `backend/tests/integration/test_tasks.py`; Modify `backend/app/main.py`, `backend/tests/integration/{test_concurrency,test_contract_review}.py`.

**Interfaces:** Produces `TaskService.create(actor_id: UUID, project_id: UUID, data: TaskCreate) -> Task`, `.get(actor_id: UUID, project_id: UUID, task_id: UUID) -> Task`, `.update(actor_id: UUID, project_id: UUID, task_id: UUID, data: TaskPatch) -> Task`, `.delete(actor_id: UUID, project_id: UUID, task_id: UUID, expected_version: int) -> None`. `validate_task_dates(start_date: date | None, due_date: date | None) -> None`, `next_completed_at(old_status: str, new_status: str, old_completed_at: datetime | None, now: datetime) -> datetime | None`는 rules.py에 둔다.

- [ ] `test_done_reentry_uses_new_time`: DONE→DONE 시 `assert completed_at == first_time`, DONE→IN_PROGRESS 시 None, 재DONE은 second_time. `test_same_version_has_one_winner`: `assert sorted(statuses) == [200,409]`, `assert version == 2`. `test_member_removal_and_assignment_never_orphan`은 두 순서 모두 assignee null·업무 보존이다.
- [ ] `uv run pytest tests/unit/test_domain_rules.py tests/integration/test_tasks.py tests/integration/test_concurrency.py -q` 실행, 신규 계약 기대 FAIL.
- [ ] T02~T05, 입력 merge 후 전체 날짜 검증, 프로젝트 밖·과거 날짜 허용, 동일 값 PATCH도 version 증가, 현재 소속 작성자/owner 삭제를 구현한다. task ID는 project 조건과 함께 조회한다. task 변경은 project.version을 올리지 않는다. 프로젝트 잠금 후 task.version·담당자 소속을 검사하고 lock/deadlock/statement timeout 오류를 정해진503으로 변환한다.
- [ ] TK-01~14, PM-18, RV-18 전체 PASS. 프로젝트 삭제/업무 생성 경합 양순서, rollback 후 새 연결로 최종 DB 검사, lock/pool 반환을 검증한다. I05~I06 회귀 PASS.
- [ ] `feat: implement versioned tasks and concurrency rules`로 커밋한다.

### I09. 업무 검색·필터·정렬 API

**Files:** Create `backend/app/tasks/queries.py`; Modify `backend/app/tasks/{schemas,service,router}.py`, `backend/app/core/{query,clock}.py`, `backend/tests/integration/test_query_contracts.py`, `backend/tests/unit/test_domain_rules.py`.

**Interfaces:** Produces `TaskService.list(actor_id: UUID, project_id: UUID, query: TaskQuery) -> Page[Task]`; `seoul_today(clock: Clock) -> date`는 core/clock.py에 추가한다. query 파서는 원본 QueryParams의 중복·unknown을 검사한 후 DTO를 생성한다.

- [ ] `test_literal_search_and_nulls_last`는 %, _, 역슬래시가 문자 그대로 검색되고 null due가 마지막임을 확인한다. `test_overdue_at_seoul_midnight`는 DONE 제외·자정 경계 정확; `test_nonmember_assignee_filter_returns_empty`는 `assert items == []`, `assert status == 200`이다.
- [ ] `uv run pytest tests/integration/test_query_contracts.py tests/unit/test_domain_rules.py -q` 실행, 신규 query 계약 기대 FAIL.
- [ ] T01의 q/status/assignee/due_from/due_to/overdue를 AND로 결합하고 count와 items에 같은 조건을 사용한다. 읽기 snapshot·허용 정렬·ID 동률·NULLS LAST를 적용한다. 범위 역전·잘못된 값·중복 query는422, page 초과는 빈200. 관련 사용자 요약을 일괄 조회하여 N+1을 막는다.
- [ ] QF-01~07 전체 PASS, page1/size20·size100/101·total0·동률·snapshot을 검사한다. 데이터 정합성 뒤 쿼리 수를 확인하며, 성능 수치 판정은 I13에서 수행한다.
- [ ] `feat: add consistent task queries and filters`로 커밋한다.

### I10. 업무 목록·보드·편집·복구 화면

**Files:** Create `frontend/src/features/tasks/{api,types,useTaskQuery,board}.ts`, `frontend/src/features/tasks/{TasksPage,TaskDetailPage,TaskForm,TaskFilters,TaskTable,TaskBoard,TaskCard,ConflictPanel}.vue`, `frontend/src/lib/useLatestQuery.ts`, `frontend/tests/component/tasks.spec.ts`; Modify `frontend/src/router/index.ts`, `frontend/src/features/projects/api.ts`, `frontend/tests/component/{query-and-errors,review-cases}.spec.ts`.

**Interfaces:** task api는 `listTasks(projectId: string, query: TaskQuery): Promise<Page<Task>>`, `getTask(projectId: string, taskId: string): Promise<Task>`, `createTask(projectId: string, input: TaskCreate): Promise<Task>`, `updateTask(projectId: string, taskId: string, input: TaskPatch): Promise<Task>`, `deleteTask(projectId: string, taskId: string, expectedVersion: number): Promise<void>`를 제공한다. GET 함수에는 마지막 선택 인자 `signal?: AbortSignal`을 추가하여 I04 래퍼에 전달한다. I07의 GET 함수도 동일하게 추가한다. `mergeBoardTasks(current: Task[], incoming: Task[]): Task[]`는 ID별 최대 version을 반환한다. `useLatestQuery<T>(loader: (signal: AbortSignal) => Promise<T>)`는 data/error/loading/reload/clear를 제공하고 요청 번호+인증 세대를 검사한다. 페이지 replace와 더 보기 append를 별도 동작으로 구분한다.

- [ ] `board_keeps_highest_version_in_actual_column`은 같은 ID version1 TODO/version2 IN_PROGRESS를 받으면 `expect(cardsForId).toHaveLength(1)` 및 IN_PROGRESS 표시. `lost_create_response_does_not_retry`는 `expect(postCalls).toBe(1)`, 결과 불명 안내·초안 유지. `conflict_requires_explicit_reapply`는 최신 GET 뒤 자동 PATCH 0회. RV-08·11, QF-11.
- [ ] `npm run test:unit -- --run tests/component/tasks.spec.ts tests/component/review-cases.spec.ts tests/component/query-and-errors.spec.ts` 실행, 신규 계약 기대 FAIL.
- [ ] S05/S06, 필터300ms debounce·IME 조합 완료 처리, 목록/보드 URL 상태, 열별20개·더 보기·독립 오류 재시도를 구현한다. 전체 재조회는 이전 컬렉션을 교체하며 writes가 멈추면 수렴한다. 날짜는 달력 문자열로 유지한다. 저장 전 중복클릭 차단, 성공 뒤 목록·집계 재조회, 409 비교·명시적 재적용, 네트워크 실패 초안 유지, 401 초안 제거, 403/404 편집 중단을 구현한다. 설명은 텍스트로 렌더링한다.
- [ ] TK-05·15, QF-08~12·14~15·18~20, RV-08·09·11의 UI 부분 PASS. 빈 상태·한 열 오류·다중 페이지·역순 응답·최대 길이 입력을 확인하고 typecheck/lint/build PASS.
- [ ] `feat: add task views and explicit write recovery`로 커밋한다.

### I11. 프록시·배포 프로필·시드·복구 도구

**Files:** Create `backend/Dockerfile`, `frontend/Dockerfile`, `backend/.dockerignore`, `frontend/.dockerignore`, `infra/nginx/{default.conf,security.conf,test-e2e.conf}`, `compose.production.yaml`, `backend/app/cli/{seed,cleanup_sessions}.py`, `scripts/{Start-TestStack,Stop-TestStack,Backup-Database,Restore-Database}.ps1`, `backend/tests/system/{test_auth_rate_limit,test_proxy_contracts}.py`, `docs/runbooks/backup-restore.md`; Modify `compose.test.yaml`, `infra/.env.example`, `docs/runbooks/local-development.md`, `backend/tests/system/test_migrations_seed.py`, `README.md`.

**Interfaces:** `uv run python -m app.cli.seed`는 APP_ENV development/test·환경 비밀번호만 받고 시드 ID 기반으로 재실행 가능하다. `uv run python -m app.cli.cleanup_sessions`는 expires_at<=now인 세션만 삭제한다. `Start-TestStack.ps1 -RunId <id> -Profile e2e|security`는 URL과 비밀 없는 manifest 경로를 반환한다. `Stop-TestStack.ps1 -Manifest <path>`는 생성한 자원 exact match만 중지·정리한다.

- [ ] `test_production_proxy_keeps_api_404_and_hides_docs`, `test_shared_auth_zone_limits_seventh_request`, `test_seed_twice_preserves_user_created_data`, `test_upgrade_and_restore_in_separate_database`를 작성한다. rate 테스트는 빈 zone에서 1초 이내7개 요청의 upstream 도달6/429 1을 검사한다. 임의 X-Forwarded-For 변경으로 예산이 늘지 않아야 한다.
- [ ] `uv run pytest tests/system -q` 실행, 누락된 proxy/CLI 동작 기대 FAIL. TLS·컨테이너 미준비는 환경 BLOCKED이며 기능 RED로 세지 않는다.
- [ ] 배포는 비root API 단일worker, 내부DB·API, Nginx 정적 파일/SPA·API 분리, TLS와 Secure 쿠키를 구성한다. `/api/docs`·OpenAPI는 dev만 제공한다. Nginx와 API 각각 body 256KiB, auth 공유 zone 5r/m burst5 nodelay·429를 적용한다. 로그 원본query·쿠키를 제외한다. 시드 두 번 실행 시 사용자 데이터 보존, 복구는 새 DB에 restore 후 검증하고 기존 DB를 덮어쓰지 않는다. image·runtime patch/digest를 기록한다.
- [ ] AU-14, RV-01·03·04·12·13·14·16 PASS. seed·cleanup·백업/복원은 fixture 대상에서 증명한다. E2E 완화 설정과 production rate 설정이 섞이지 않았는지 검사한다. G0 전체 조건을 재확인한다.
- [ ] `feat: package same-origin deployment and reproducible demo data`로 커밋한다.

### I12. 실제 연결 E2E·브라우저·접근성

**Files:** Create `frontend/playwright.config.ts`, `frontend/tests/e2e/{core-journeys,write-recovery,layout-accessibility,browser-input}.spec.ts`, `frontend/tests/e2e/fixtures.ts`, `backend/tests/support/seed_e2e.py`; Modify `frontend/package.json`, `frontend/package-lock.json`, `backend/tests/integration/test_contract_review.py`.

**Interfaces:** fixture `actors`는 A/B/C의 독립 BrowserContext, `seed`는 해당 run DB의 project/task ID만 제공한다. UI·API 모킹 없는 여정과 응답 유실 주입 여정을 분리한다. 테스트 provisioner 인증정보는 브라우저 번들에 넣지 않는다.

- [ ] E2E-01~05를 테스트 계획 9.1 그대로 구현한다. E2E-03의 `expect(draft).toHaveValue(originalDraft)`, 명시적 재적용 전 PATCH 증가0; E2E-05에서 재시작 후 업무 존재, P 삭제 후 Q 업무 유지. RV-17에서 이모지·한글 IME·날짜·붙여넣기·키보드를 검증한다.
- [ ] frontend `npm run test:e2e -- --project=chromium` 실행. 이미 완성된 기능이면 처음부터 PASS일 수 있으므로 인위적으로 실패시키지 않는다. 실패는 재현·수정 테스트를 추가하고 해당 코드만 고친다.
- [ ] 360×800/768×1024/1440×900, 200% 확대, focus trap/복귀, 첫 오류 포커스, 색 외 상태 표시를 검증한다. 자동 접근성 critical/serious0과 수동 키보드 여정 성공을 기록한다. OpenAPI의 21 endpoint·역할 행렬·과잉 필드 거부를 RV-15로 대조한다.
- [ ] 전체 Playwright Chromium/Firefox/WebKit 행렬, backend 통합 계약, 프론트 정적 검사 PASS. 기본 retry0, flaky를 PASS로 바꾸지 않는다. QF-16~17·19, RV-11·15·17, E2E-01~05 증거 확보. 테스트 엔진·버전을 보고한다.
- [ ] `test: verify complete user journeys and browser interactions`로 커밋한다.

### I13. CI·성능·최종 증거·포트폴리오 설명

**Files:** Create `.github/workflows/mvp-ci.yml`, `tests/performance/locustfile.py`, `tests/performance/README.md`, `docs/verification/mvp-results.md`, `docs/verification/traceability.csv`, `scripts/Verify-Traceability.ps1`; Modify `backend/pyproject.toml`, `backend/uv.lock`, `README.md`, `docs/runbooks/local-development.md`.

**Interfaces:** traceability.csv columns는 `id,task,test_path,test_name,status,evidence,commit`이다. Verify-Traceability는 AU15/PM18/TK15/QF20/RV18, E2E5, PERF4의 정의 누락·중복·증거 없는 PASS를 거부하고 exit1을 반환한다. 성능 의존성은 별도 performance group, pytest 도구는 기본 dev group이다.

- [ ] 추적 검증기에 RV-18 누락·증거 없는 PASS fixture를 주어 exit1, 완전한 NOT_RUN 목록은 구조 검증 성공이나 release gate 실패를 기대한다. Locust 시나리오에서 응답 status·전체 지연·성공 지연을 따로 집계한다.
- [ ] CI는 lock 설치→lint/type/unit/component→PostgreSQL 통합→빌드→E2E/보안proxy 순서로 구성한다. 외부배포 job은 만들지 않는다. 성능·30회 동시성 후보 검증은 명시적 candidate job으로 분리하고 결과 artifact에 commit·lock hash·DB/runtime 버전을 담는다.
- [ ] PERF-01~04를 전용4vCPU/8GB·singleworker·1000tasks·10VU에서 warmup10초 후60초 측정한다. 요청 간250ms·최소200표본, list p95≤500ms/일반write≤1초 목표, contention의409/503와 최대본문 stress는 별도 보고한다. 조건 미충족·표본 부족은 명시하고 수치를 만들어 넣지 않는다.
- [ ] 전체 기능86 ID, E2E5, PERF4를 실제 증거에 연결한다. P0/P1 FAIL/BLOCKED/FLAKY0, 무결성 위반0, 미해결 범위 변경0을 요구한다. 성능 미달이면 원인·대응을 기록하고 사용자 검토 없이 목표를 낮추지 않는다. 읽기 snapshot·보드 일시 불일치·세션·정확한 이메일 추가의 한계를 README에 설명한다.
- [ ] 문서·정적 검사·변경 후 관련 회귀 PASS 후 `ci: enforce MVP acceptance gates and record verification`로 커밋한다. 실행 방식에 맞는 전체 변경 리뷰를 받고, 실제 통과 범위와 남은 제약을 보고한다.

## 4. 요구사항·검증 소유자

여러 작업에 걸치는 ID는 전체 증거가 모였을 때만 PASS다. 아래 범위는 정의가 아니라 기존 테스트 계획의 실행 책임 배정이다.

| 계약·검증 | 구현 책임 |
|---|---|
| H01~H02 | I01·I02·I11 |
| A01~A04 / AU-01~12·15 | I03; AU-04 프론트 I04 |
| AU-13 / AU-14 | I04 / I11 |
| P01~P05 / PM-01~06·16~17 | I05; 제약 I02 |
| M01~M05 / PM-07~15·18 | I06; PM-18 업무삭제 I08 |
| T02~T05 / TK-01~14 | I08; TK-03 제약 I02 |
| T01 / QF-01~07 | I05·I09 |
| TK-15 / QF-08~11·14 | I10·I12 |
| QF-12·15 | I07·I10 |
| QF-13·18~20 | I04·I10·I12 |
| QF-16~17 | I12 |
| RV-01·03·04·12·16 | I11 |
| RV-02·05·07·14 | I03; RV-14 프록시 I11 |
| RV-06 / RV-13 | I01 / I01·I02·I11 |
| RV-08·11 / RV-09 / RV-10 | I10·I12 / I04·I10 / I05 |
| RV-15·17 / RV-18 | I12 / I06·I08 |
| E2E-01~05 / PERF-01~04 | I12 / I13 |

## 5. 계획 자체의 검토와 인계

설계의 기능10개·업무/health API21개·화면S01~S09에 작업을 배정했다. 프로젝트 삭제와 멤버 제거의 대량변경, 읽기 Session과 인증 Session 분리, nullable 날짜 PATCH, 같은 값 수정의 버전 증가, production 프록시까지 실행 책임을 지정했다. Review Focus의 5개 실패 조건은 각각 구체적 테스트 작업에 들어 있다.

각 기능은 자신의 TDD 주기를 가진다. 설치·문서·이미 동작하는 최종 통합 테스트에는 의미 없는 실패를 만들지 않는다. 신규 기능은 실제 기대 동작의 실패→구현→통과를 확인한다. 이 계획의 검토 완료와 제품 완료는 별개다.

실행 방식 제안은 Native다. 13개 작업이 DB transaction·DTO·오류·인증 상태 계약을 이어 사용하므로 같은 실행자가 순서대로 구현하고 마지막에 독립 리뷰를 받으면 맥락 전달 비용이 작다. 작업마다 독립 검토를 원하면 Subagent-driven을 선택할 수 있다. 어느 방식이든 시작 전에 문서 검토 결과와 실행 방식을 확인하고 체크박스·실제 증거를 갱신한다.
