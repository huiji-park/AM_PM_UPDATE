# AM_PM MVP Environment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Windows에서 개발·통합 테스트·브라우저 테스트·시연 빌드를 재현할 수 있는 환경을 마련한다.

**Architecture:** Python과 Node는 Windows 호스트에서 실행하고 PostgreSQL은 Docker Desktop의 Linux container로 실행한다. 개발은 Vite 프록시, 통합·보안 검증은 별도 Compose 스택을 사용한다. production 구성은 같은 출처 HTTPS Nginx·내부 API·내부 DB로 분리한다.

**Tech Stack:** Python 3.13 + uv, Node.js 24 LTS + npm, PostgreSQL 18, Docker Desktop/Compose v2, PowerShell, Git.

**Spec:** [상세 설계 9절](../specs/2026-10-03-am-pm-mvp-design.md), [제약 검토](../specs/2026-10-03-am-pm-mvp-constraints-review.md), [테스트 계획](2026-10-03-am-pm-mvp-test-plan.md), [구현 계획](2026-10-04-am-pm-mvp-implementation-plan.md).

작성일: 2026-10-04, Asia/Seoul. 상태: 설치·설정 실행 전. 아래 명령은 구현 단계의 절차이며 지금 실행한 결과가 아니다. 프로젝트 manifest·Compose·스크립트는 구현 계획의 소유 작업에서 만든다. 이 문서는 같은 파일을 별도로 중복 구현하지 않는다.

## Global Constraints

- 앱 Python은 3.13, DB는 PostgreSQL 18, API는 단일 worker다.
- 개발 출처는 `http://localhost:5173`, API base는 `/api/v1`, API 포트는 8000이다.
- 개발 쿠키는 Secure=false, production은 HTTPS·Secure=true다.
- DB pool은 5+5, 획득·잠금 제한은 각각 3초, SQL 제한은 5초다.
- SESSION_TTL_SECONDS=604800, APP_TIMEZONE=Asia/Seoul, LOG_LEVEL=INFO다.
- 실제 `.env`, TLS 개인키, DB dump, 테스트 trace, 비밀번호는 Git에 넣지 않는다.
- 개발 DB·테스트 DB는 다른 인스턴스와 volume을 사용한다.
- 앱 startup은 schema를 자동 생성하지 않고 Alembic을 별도 실행한다.
- 기존 Client/Server·사용자 DB·시스템 Node 설치를 임의로 삭제·초기화하지 않는다.

## Review Focus

1. 기존 Node가 PATH에서 먼저 선택됨: E02에서 실행 경로와 major24를 함께 확인한다.
2. localhost와 127.0.0.1을 브라우저 주소에서 혼용함: E05에서 Origin·쿠키·Swagger를 같은 출처로 검증한다.
3. Compose override가 개발 DB volume을 테스트에 재사용함: E03·E06에서 독립 compose 파일·project 이름·manifest를 검사한다.
4. PostgreSQL major18의 데이터 경로를 이전 버전처럼 설정함: E03에서 volume mount와 재시작 후 데이터 유지 확인을 배정한다.
5. 설치는 되었지만 Docker engine·DB schema가 준비되지 않음: E03·E05에서 engine/DB/ready를 각각 검사한다.

## 1. 현재 확인된 것과 목표

직전 문서 작성 단계에서 PATH와 기본 실행 경로를 읽기 전용으로 확인했다. 실행 착수 시 E01에서 다시 확인한다.

| 항목 | 관찰 | 목표·대응 |
|---|---|---|
| 저장소 | AM_PM, 원본 Client/Server와 문서 존재 | 별도 구현 worktree 사용 |
| shell/Git | PowerShell, Git 사용 가능 | Git 설정은 프로젝트 범위로 적용 |
| Node/npm | Node v20.9.0, node/npm PATH에 존재 | 프로젝트 세션에서 Node 24 LTS 선택 |
| Python/py/uv | PATH에서 발견되지 않음 | uv와 Python 3.13 설치 또는 기존 설치의 경로 확인 |
| docker/psql | PATH에서 발견되지 않음 | Docker CLI/engine 확인; psql은 DB container 안에서 사용 가능 |
| Docker Desktop | 기본 exe 경로에서 발견되지 않음 | 실제 설치 여부 확인 후 필요 시 설치 |
| WSL | wsl.exe 경로 존재 | 버전·가상화·Linux engine은 미확인 |
| 신규 backend/frontend | 없음 | I01/I04에서 생성·lock |
| Codex 번들 Python | 문서 처리에 사용 가능 | 프로젝트 런타임으로 고정하지 않음 |

PATH 미발견은 미설치 확정이 아니다. 버전·CPU 아키텍처·OS 지원·여유 메모리를 확인한 뒤 맞는 배포본을 선택한다. 현재 Vite 문서의 Node 하한은 20.19+ 또는22.12+이고, Node 공식 릴리스 표에서24는 LTS다. 이 프로젝트는24를 기준으로 한다. [Vite 요구사항](https://vite.dev/guide/), [Node 릴리스](https://nodejs.org/en/about/previous-releases)

## 2. 버전·의존성 정책

| 대상 | 고정 위치 | 선택 규칙 |
|---|---|---|
| Python | backend/.python-version | 3.13 계열의 설치한 정확한 patch 기록 |
| Python 패키지 | backend/pyproject.toml + uv.lock | 최초 호환 버전 해석 후 lock, 재현 설치는 --frozen |
| uv | docs/runbooks/local-development.md | 실제 사용한 uv 버전 기록, CI도 동일 버전 |
| Node | frontend/.node-version | 24 LTS의 설치한 정확한 patch 기록 |
| npm | frontend/package.json의 packageManager + runbook | 선택 Node와 검증한 npm 버전 기록 |
| 프론트 패키지 | package.json + package-lock.json | 최초 npm install, 이후 npm ci |
| DB·Nginx·빌드 이미지 | 각 Dockerfile/Compose | 호환 patch와 digest 확인 후 고정, latest 금지 |
| Playwright | package-lock + 설치 browser revision | 패키지와 브라우저를 함께 갱신 |

지금 확인되지 않은 patch 번호를 임의로 적지 않는다. 최초 설치 후 version 출력·lock hash·이미지 digest를 기록해야 환경 준비 완료다. 업데이트는 잠금 파일 diff와 관련 테스트를 함께 검토한다.

백엔드 런타임 의존성: fastapi, uvicorn, pydantic-settings, sqlalchemy, psycopg[binary], alembic, pwdlib[argon2], email-validator, tzdata. tzdata는 Windows에서도 ZoneInfo('Asia/Seoul')을 재현하기 위해 포함한다. 개발 그룹: pytest, httpx, ruff, mypy 및 실제 필요한 typing 지원. 성능 그룹: locust. 프론트 런타임은 vue/vue-router/pinia, 개발 의존성은 vite·Vue plugin·typescript·vue-tsc·eslint·Vitest·Vue Test Utils·jsdom·Playwright·접근성 검사 도구다. Redis·Celery·외부 UI framework는 추가하지 않는다.

## 3. 환경·포트·데이터 배치

| 환경 | 호스트 접근 | DB·수명 | 프로필 |
|---|---|---|---|
| 개발 | localhost:5173, API localhost:8000 | localhost:5433 / ampm_dev, 보존 volume | APP_ENV=development |
| 통합 테스트 | DB localhost:5434; 앱은 테스트 프로세스 | ampm_test_<run_id>_<worker_id>, 실행별 폐기 | APP_ENV=test |
| E2E | http://localhost:8081 | 독립 test DB, seed별 격리 | test, 기능용 auth 제한 완화 |
| 보안 프록시 | https://localhost:8443 | 독립 test DB·빈 rate zone | production 설정 검증, 실제 제한 |
| 시연 배포 구성 | 도메인:443만 공개, 80은 HTTPS 리다이렉트 선택 | 내부5432, 내부API8000, 보존 volume | APP_ENV=production |

개발 Compose project는 `ampm-dev`, 테스트는 `ampm-test-<run_id>`, production 로컬 검증은 별도 project다. test/production Compose는 각각 standalone 파일로 만든다. 개발 파일 위에 단순 병합하여 volume/ports가 남는 형태를 피한다. 포트가 사용 중이면 소유 프로세스를 확인하고 사용자 앱을 종료하지 않은 채 대체 포트를 정한다. 바뀐 출처는 PUBLIC_ORIGIN·프록시·테스트 baseURL에 함께 반영한다.

## 4. 설정 파일 계약

| 파일 | 내용·로딩 |
|---|---|
| backend/.env.example | 키와 비밀 없는 예시, DATABASE_URL은 빈 값으로 제공 |
| backend/.env | 로컬 앱 설정, backend 작업 디렉터리에서 Settings가 로딩 |
| infra/.env.example | POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD 등의 이름·설명 |
| infra/.env.local | 개발 Compose용 값; compose --env-file로 명시 |
| test manifest | 실행 ID·allowlist·DB 목록·resource 이름, 비밀은 별도 권한 제한 파일/환경 |
| frontend | API 경로는 /api/v1; 비밀 환경변수 불필요 |

backend 환경 값:

```dotenv
APP_ENV=development
DATABASE_URL=
PUBLIC_ORIGIN=http://localhost:5173
COOKIE_SECURE=false
SESSION_TTL_SECONDS=604800
APP_TIMEZONE=Asia/Seoul
LOG_LEVEL=INFO
```

DATABASE_URL은 `postgresql+psycopg` 형식으로 해당 앱 계정의 실제 값을 넣는다. 특수문자 비밀번호는 URL 인코딩하고 로그·명령행 출력에 노출하지 않는다. `.env` 파일은 자동 상속된다고 가정하지 않고 작업 위치·로딩 규칙을 고정한다. Alembic은 같은 Settings를 사용한다. production은 서버 secret 환경에서 주입한다.

개발은 bootstrap 계정으로 DB를 초기화한 뒤 앱용 계정과 migration용 계정을 분리한다. 테스트 provisioner는 독립 test 인스턴스의 DB 생성 권한만 사용한다. 일반 앱 계정에는 CREATE DATABASE·superuser를 주지 않는다. migration은 migration 계정의 DATABASE_URL로만 실행하고 앱은 앱 계정으로 재전환한다. 앱에는 업무 테이블 DML과 readiness용 alembic_version SELECT를 부여하고 migration의 default privileges로 새 테이블에도 적용한다. POSTGRES_USER는 bootstrap 계정이며 앱 계정과 동일하다고 가정하지 않는다.

추가 개발 명령 입력 `SEED_PASSWORD`는 실행할 때만 공급하고 최소12 코드 포인트 검증을 적용한다. cleanup_sessions는 정상 앱 DB 설정을 사용하지만 만료행만 제거한다. 테스트 전용 `TEST_RUN_ID`, `TEST_RUN_TOKEN`, `TEST_ADMIN_DATABASE_URL`, `TEST_ALLOWED_HOSTS`는 제품 Settings와 분리하여 tests/support/database.py가 읽는다. 토큰·관리자 URL은 보고서에 기록하지 않는다.

## 5. 작업별 준비 절차

### E01. 사전 확인·작업 위치

**Files:** I01에서 만들 `docs/runbooks/local-development.md`에 실제 결과를 기록한다. 구현 계획에서는 최종 runbook 완성을 I11이 소유하므로 I01에서는 환경 확인 절만 먼저 만든다.

**Interfaces:** 입력=현재 PC와 저장소, 출력=런타임 경로·버전·OS/아키텍처·포트 사용·작업 checkout 경로.

- [ ] 저장소 변경을 읽고 구현 작업용 격리 checkout을 마련한다. 원본 Client/Server의 수정 여부를 기준점에 기록한다.
- [ ] 아래 읽기 명령으로 경로·버전·WSL 상태를 확인한다. 없는 명령은 실패 사실을 기록하고 다음 설치 단계에서 다룬다.

```powershell
Get-Command git,node,npm,python,py,uv,docker,psql,wsl -ErrorAction SilentlyContinue
git --version
node --version
npm.cmd --version
wsl --version
wsl --status
Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object LocalPort -In 5173,8000,5433,5434,8081,8443
```

- [ ] 완료 기준: 필요한 작업을 설치/경로변경/이미충족/외부제약으로 구분한다. E01은 테스트 통과나 환경 설치 완료가 아니다.

### E02. uv·Python·Node 준비

**Files:** I01 backend/.python-version, I04 frontend/.node-version, runbook에 버전 기록. 사용자 PATH의 영구 변경은 기본값이 아니다.

**Interfaces:** 입력=E01 결과, 출력=`uv run python`의3.13·Node24·npm·uv를 사용할 수 있는 프로젝트 shell.

- [ ] uv가 없으면 WinGet에서 패키지를 확인한 후 설치한다. WinGet을 쓸 수 없으면 공식 uv release archive를 내려받아 사용자 도구 디렉터리에 풀고 해당 shell에서만 PATH 앞에 둔다. 시스템 ExecutionPolicy 변경은 하지 않는다. [uv 공식 설치법](https://docs.astral.sh/uv/getting-started/installation/)

```powershell
winget show --id astral-sh.uv -e
winget install --id astral-sh.uv -e
uv --version
uv python install 3.13
uv python find 3.13
```

- [ ] Python 설치가 끝나면 정확한3.13 patch를 확인하고 I01에서 `.python-version`에 pin한다. 전역 python 명령의 존재보다 프로젝트의 `uv run python --version`이 맞는지가 기준이다. [uv Python 관리](https://docs.astral.sh/uv/guides/install-python/)
- [ ] Node 공식 다운로드에서 OS/아키텍처에 맞는24 LTS ZIP과 checksum을 확인한다. 프로젝트 전용 사용자 도구 폴더에 추출하고 shell PATH 앞에 그 bin 경로를 설정한다. 기존20.9 설치를 제거하지 않는다. `.node-version` 파일만으로 Windows PATH가 바뀌지는 않는다.
- [ ] `Get-Command node,npm`, `node --version`, `npm.cmd --version`으로 새 경로와 major24를 확인한다. npm.ps1 정책 문제는 npm.cmd로 실행한다. version 출력과 checksum·설치 출처를 runbook에 기록한다.
- [ ] 완료 기준: Python3.13/Node24/uv/npm이 의도한 경로에서 실행된다. 다운로드/OS 권한 문제는 실제 제약으로 기록하고 자동으로 전역 정책을 완화하지 않는다.

### E03. Docker·개발/테스트 PostgreSQL

**Files:** I01 compose.yaml/compose.test.yaml, 추가 `infra/.env.example`, `infra/postgres/bootstrap.sql`, `infra/postgres/bootstrap.sh`, `.gitignore`, runbook. bootstrap.sh가 환경값을 psql 변수로 전달하고 bootstrap.sql은 식별자·값을 각각 올바르게 quoting한다. 비밀번호를 파일에 직접 넣거나 shell tracing으로 출력하지 않는다.

**Interfaces:** 입력=Docker Linux engine, 출력=개발 localhost5433와 별도 테스트 localhost5434의 PostgreSQL18. bootstrap→migration→app 권한 경계와 named volume이 확인되어야 한다.

- [ ] Docker Desktop 설치 여부·OS/가상화 지원을 확인한다. WSL backend는 공식 요구사항의 WSL2.1.5 이상 등을 점검한다. 필요한 OS 기능 설정·관리자 작업·재부팅은 실행 시 사용자의 PC 상태를 확인하고 진행한다. [Docker Windows 설치 조건](https://docs.docker.com/desktop/setup/install/windows-install/)
- [ ] 설치가 필요하면 공식 Docker Desktop installer로 Linux container/WSL backend를 선택한다. `docker version`, `docker compose version`, `docker info --format '{{.OSType}}'`에서 server 응답·Compose v2·linux를 확인한다. CLI 존재만으로 engine 준비를 통과시키지 않는다.
- [ ] I01에서 Compose와 .env.example을 만든 후 infra/.env.local에 새 개발 전용 자격증명을 공급한다. 비밀이 치환된 `docker compose config` 전체를 로그에 출력하지 않고 `config --quiet`로 검증한다.
- [ ] PostgreSQL18 image는 named volume을 `/var/lib/postgresql`에 마운트한다.18부터 기본 PGDATA는 버전별 하위 경로를 사용하므로 이전 major의 mount 경로를 복사하지 않는다. init 환경변수는 빈 데이터 디렉터리 초기화에만 적용된다. [공식 PostgreSQL 이미지](https://hub.docker.com/_/postgres)

```powershell
docker compose --env-file infra/.env.local -p ampm-dev -f compose.yaml config --quiet
docker compose --env-file infra/.env.local -p ampm-dev -f compose.yaml up -d db
docker compose --env-file infra/.env.local -p ampm-dev -f compose.yaml ps
```

- [ ] DB 컨테이너 안의 psql로 SELECT1·server_version·SHOW timezone을 검사한다. healthcheck는 pg_isready, 실제 준비 검증은 앱 계정의 SQL 접속이다. 테스트 DB는 독립 compose project·volume·자격증명으로 시작하고 guard가 wrong host/name/env/token을 거부하는지 I01 테스트로 확인한다.
- [ ] 완료 기준: 두 인스턴스가 분리되고 DB major18, 앱 계정 superuser=false, 개발 DB 테스트행이 container 재시작 뒤 유지된다. 임의 test행은 해당 행만 제거한다. Docker 사용 불가 시 별도 PostgreSQL18 서버를 사용할 수 있지만 전체 proxy/E2E 검증은 가능한 CI 환경을 마련하기 전까지 BLOCKED다. SQLite로 대체하지 않는다.

### E04. 프로젝트 manifest·의존성 설치

**Files:** 구현 I01 backend manifest·lock, I04 frontend manifest·lock·검사 스크립트.

**Interfaces:** 입력=E02 런타임, 출력=재현 가능한 설치와 코드 검사 명령. 제품 코드는 각 기능 작업에서 작성한다.

- [ ] backend pyproject의 requires-python은 `>=3.13,<3.14`로 하고 runtime/dev/performance를 구분한다. 첫 lock 생성 후 검토하고 commit한다. 신규 lock 생성은 `uv lock`; 일반 설치는 다음 명령이다.

```powershell
# backend 작업 디렉터리
uv sync --frozen
uv run python --version
uv run ruff check .
uv run mypy app
```

- [ ] frontend의 Node engines는24계열로 정하고 strict TS·lint·Vitest·Vite scripts를 I04에 넣는다. 초기는 `npm.cmd install`로 lock을 만들고, 그 뒤부터는 다음 명령을 쓴다.

```powershell
# frontend 작업 디렉터리
npm.cmd ci
npm.cmd run typecheck
npm.cmd run lint
npm.cmd run test:unit -- --run
npm.cmd run build
```

- [ ] lock 기반 재설치 뒤 lock diff가 없어야 한다. .venv/node_modules/dist/reports/playwright-report/test-results·환경파일·도구ZIP은 ignore하고 `.env.example`만 추적한다. `.gitattributes`는 코드 LF를 기본으로 하고 PowerShell 사용성을 확인한다.
- [ ] 완료 기준: manifest와 실제 runtime이 일치하고 해당 시점에 구현한 기능의 정적·단위 검사가 통과한다. 아직 구현하지 않은 후속 기능을 통과로 표기하지 않는다.

### E05. 마이그레이션·로컬 실행·같은 출처 확인

**Files:** I02 migration, I03 auth, I04 Vite, I11 seed가 생기는 순서에 맞춰 runbook을 갱신한다.

**Interfaces:** 입력=독립 개발 DB와 manifest, 출력=localhost5173에서 실제API·DB와 연결된 개발 앱.

- [ ] backend/.env를 작성하고 migration용 DB 권한으로 아래 명령을 실행한다. 첫 실행은 빈 DB upgrade이며 migration 실패 시 앱을 시작하지 않는다.

```powershell
# backend 작업 디렉터리
uv run alembic upgrade head
uv run alembic current
```

- [ ] 앱 계정 DATABASE_URL로 전환하고 backend·frontend를 별도 terminal에서 시작한다. 두 프로세스의 로그를 구분하고 브라우저는 localhost5173으로만 연다.

```powershell
# backend terminal
uv run uvicorn app.main:create_app --factory --host localhost --port 8000 --reload
```

```powershell
# frontend terminal
npm.cmd run dev -- --host localhost --port 5173 --strictPort
```

- [ ] localhost8000/health/live·ready 각각200, localhost5173/api/docs 접근, 문서에서 로그인·쓰기 요청에 X-AMPM-Request 헤더를 넣어 성공하는지 검사한다. Swagger 요청에 헤더를 넣을 수 있도록 OpenAPI parameter 또는 requestInterceptor를 구현한다. 브라우저가 Origin을 공급한다. 8000의 문서 직접 호출을 위해 허용 Origin을 넓히지 않는다.
- [ ] I11의 seed CLI가 만들어지면 development에서 비밀번호를 환경으로 공급하고 `uv run python -m app.cli.seed`를 두 번 실행한다. 같은 데이터 수·사용자 생성 데이터 보존을 확인한다. 일반 개발자가 데이터가 있는 DB에 downgrade를 실행하는 절차는 넣지 않는다.
- [ ] 완료 기준: 가입→로그인→새로고침→프로젝트 조회까지 해당 구현 구간의 smoke 성공, localStorage 토큰 없음, 쿠키 Path/HttpOnly/SameSite 확인. API 재시작 후 세션·데이터 유지 확인은 I12에서 증거화한다.

### E06. 테스트·TLS·시연 후보 실행

**Files:** I11 scripts·standalone Compose·TLS설정, I12 Playwright, I13 CI·보고서.

**Interfaces:** 입력=실행 ID·잠금 파일·대상 commit, 출력=각 test stack manifest와 검증 artifact. 구현 계획의 Start-TestStack/Stop-TestStack 인터페이스를 사용한다.

- [ ] 통합 테스트는 tests/support/database.py가 실행 ID/worker별 DB를 만들고 schema를 올린다. fixture가 admin 연결을 앱에 재사용하지 않도록 검사한다. backend에서 `uv run pytest tests/unit tests/integration -q --junitxml=reports/integration.xml`로 실행한다.
- [ ] E2E용 스택은 Start-TestStack으로 run별 project를 만들고 frontend에서 `npm.cmd exec playwright install chromium firefox webkit`, `npm.cmd run test:e2e`를 실행한다. 브라우저 설치는 lock에 맞는 revision을 사용한다. 누락된 로컬 엔진은 CI에서 확인하고 검증 범위를 보고한다.
- [ ] 보안 스택은 별도8443 프록시·빈 zone을 사용한다. 테스트용 localhost SAN 인증서를 생성해 Git 밖 파일로 mount한다. 테스트 프로세스의 신뢰 저장소만 지정하고 시스템 신뢰 저장소를 자동 변경하지 않는다. 인증서 검증을 끈 일반 E2E 결과를 HTTPS 검증 증거로 쓰지 않는다. 신뢰된 TLS 연결에서 Secure·__Host 쿠키·Origin·rate·body·SPA/API 분리를 확인한다.
- [ ] backend `uv run pytest tests/system -q --junitxml=reports/system.xml` 실행. production 설정에서는 COOKIE_SECURE=true·HTTPS PUBLIC_ORIGIN·docs 비공개를 강제하고 test용 rate 완화가 유입되지 않도록 검사한다. API/DB 포트는 외부 publish하지 않는다.
- [ ] 후보 commit에서 전체 브라우저·30회 동시성·성능 시나리오를 실행한다.4vCPU/8GB를 확보하지 못하면 비교 환경 차이를 명시한다. GitHub Actions는 Linux runner·PostgreSQL18·동일lock·독립test stack을 사용한다. workflow 작성과 원격 push/실행은 구분한다.
- [ ] 완료 기준: 테스트 계획 G0~G5의 증거를 기록하고 NOT_RUN/BLOCKED를 PASS로 바꾸지 않는다. 외부 도메인·호스팅은 실제 공개 배포를 요청할 때 결정한다.

## 6. 중지·정리·백업·복원

개발 프로세스는 해당 terminal의 Ctrl+C로 종료한다. 개발 DB를 중지할 때는 아래처럼 지정 project만 down하고 volume을 보존한다.

```powershell
docker compose --env-file infra/.env.local -p ampm-dev -f compose.yaml down
```

테스트 정리는 Stop-TestStack에 이번 실행의 manifest를 넘겨 수행한다. project·host·name·token 검증을 통과한 이번 테스트 자원만 대상으로 한다. 전체 Docker prune, 개발 down -v, 넓은 파일패턴 삭제를 기본 절차로 넣지 않는다. 실패 artifact를 먼저 수집하고 정리한다.

I11 Backup-Database는 PostgreSQL18 pg_dump의 custom format을 container 내부 파일로 생성하고 `docker cp`로 workspace의 ignored backup 디렉터리에 복사한다. PowerShell의 바이너리 파이프 리다이렉트에 의존하지 않는다. dump는 암호·개인 데이터가 포함될 수 있는 로컬 산출물로 취급한다. 성공 exit·파일크기·SHA256·schema revision을 기록한다.

Restore-Database는 새 이름의 별도 DB에 pg_restore하고 Alembic revision·제약·행 수·smoke를 확인한다. 원본 DB URL을 인자로 받으면 덮어쓰기 대신 거부한다. 복원 성공 후 앱 연결 전환은 별도 명시적 단계다. rollback은 schema 호환성 확인 없는 이전 코드 재실행으로 대체하지 않는다.

## 7. 흔한 장애와 확인 순서

| 증상 | 확인 순서·해결 범위 |
|---|---|
| Vite engine 불일치 | Get-Command node →24 경로 선택 →npm ci; lock 임의 삭제 금지 |
| uv 명령 없음 | 설치 위치 확인 →현재 shell PATH 반영 →새 terminal; 시스템 전체 정책 변경 금지 |
| Docker daemon 연결 실패 | Desktop engine 상태 →WSL 버전/가상화 →Linux mode; DB 결함으로 오판하지 않음 |
| DB 인증 실패 | 실제 선택 .env·앱 role·포트 확인; 기존 volume의 비밀번호는 env 변경만으로 바뀌지 않음 |
| ready503/live200 | DB SELECT1 →Alembic head →권한/timeout 확인; 로그에 URL 출력 금지 |
| 모든 POST403 | 브라우저 주소·PUBLIC_ORIGIN·헤더 확인; CORS 전체 허용으로 우회하지 않음 |
| 로그인 직후401 | 쿠키 이름·Secure·Path·host 확인; HTTP 개발에서 production 쿠키 오설정 검사 |
| query/mutation503 | pool 획득·행 잠금·SQL 제한을 분리; 무조건 timeout 증가·쓰기 retry 금지 |
| 테스트 bootstrap 거부 | run token/host allowlist/DB 이름/APP_ENV 확인; guard를 끄지 않음 |
| docs404 | production이면 정상; dev Vite /api proxy·문서 경로 확인 |

## 8. 환경 완료 체크리스트

- [ ] 실제 Python·uv·Node·npm·Docker·DB 버전과 경로를 기록했다.
- [ ] lock 기반 설치가 재현되며 lock의 예기치 않은 변경이 없다.
- [ ] 개발·테스트 데이터, 앱·migration·provisioner 권한이 분리된다.
- [ ] 빈 DB upgrade, disposable DB round trip, seed 재실행이 검증되었다.
- [ ] 개발 동일 출처와 production HTTPS 정책을 각각 검증했다.
- [ ] Windows 명령·Linux CI·브라우저 검증의 차이를 기록했다.
- [ ] 개발 데이터 보존 중지와 이번 실행만 정리하는 테스트 cleanup이 확인되었다.
- [ ] 새 DB에 백업 복원을 검증했다.
- [ ] 비밀값은 Git·프론트 번들·응답·로그에 없다.
- [ ] 실제 실행 증거를 구현 계획 I13 보고서에 연결했다.

현재는 계획 문서 작성만 완료한 상태다. 위 체크리스트에는 아직 완료 표시를 하지 않는다.
