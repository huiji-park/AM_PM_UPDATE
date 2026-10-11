# AM_PM MVP — 시스템 구조도·화면 흐름도

- 작성일: 2026-10-03
- 상태: 상세 설계 0.2에 대한 시각화, 사용자 검토용
- 기준: [상세 설계](2026-10-03-am-pm-mvp-design.md), [API 계약](2026-10-03-am-pm-mvp-api-contract.md), [화면 명세](2026-10-03-am-pm-mvp-screens-verification.md)

그림은 구현할 시스템의 구조를 표현한다. 현재 배포된 서비스 구성을 의미하지 않는다. 프로젝트·팀원·업무 모듈은 하나의 FastAPI 프로세스 안에 있으며 독립 서비스가 아니다.

## 1. 전체 시스템 구조

```mermaid
flowchart TB
    subgraph client[사용자 브라우저]
        vue["Vue 3 + TypeScript<br/>화면 · 폼 · 목록 · 보드"]
        state["Vue Router / Pinia<br/>화면 이동 · 인증 상태"]
        vue --- state
    end

    subgraph server[동일 출처 서버]
        nginx["Nginx<br/>HTTPS · 경로 분기 · 인증 요청 제한"]
        static["프론트 빌드 결과<br/>HTML · JS · CSS"]
        subgraph api[FastAPI 단일 애플리케이션]
            guard["요청 검증<br/>Origin · 입력 · 세션"]
            auth["인증 모듈<br/>가입 · 로그인 · 로그아웃"]
            project["프로젝트·팀원 모듈<br/>소속 · 소유권 · 팀원 관리"]
            task["업무 모듈<br/>담당자 · 상태 · 기간"]
            dbaccess["SQLAlchemy + psycopg<br/>조회 · 잠금 · 트랜잭션"]
        end
        db[(PostgreSQL)]
        migration["Alembic<br/>스키마 변경 이력"]
    end

    vue -->|"/api/v1 · 세션 쿠키"| nginx
    nginx -->|"API 요청"| guard
    nginx -->|"화면 요청"| static
    static -.->|"브라우저에서 실행"| vue
    guard --> auth
    guard --> project
    guard --> task
    auth --> dbaccess
    project --> dbaccess
    task --> dbaccess
    dbaccess --> db
    migration -.->|"배포 전 실행"| db
```

브라우저는 동일 출처의 `/api/v1`만 호출한다. HttpOnly 쿠키는 브라우저가 전달하며 Vue 코드가 세션 토큰을 읽거나 저장하지 않는다. 개발 환경에서는 Nginx의 API 전달 역할을 Vite proxy가 수행한다.

## 2. 업무 수정 요청의 처리 흐름

```mermaid
sequenceDiagram
    actor U as 팀원
    participant V as Vue 화면
    participant R as FastAPI 라우터
    participant A as 인증 의존성
    participant S as 업무 서비스
    participant D as PostgreSQL

    U->>V: 업무 상태 변경
    V->>R: PATCH + expected_version + 쿠키
    R->>R: 출처·헤더·입력 검증
    R->>A: 현재 사용자 확인
    A->>D: 토큰 해시·만료 시각 조회
    D-->>A: 유효한 세션과 사용자 ID
    A-->>R: 사용자 ID
    R->>S: 검증된 입력 전달
    S->>D: 트랜잭션 시작·프로젝트 행 잠금
    S->>D: 최신 소속·업무·버전 확인
    alt 비소속 또는 권한 없음
        S->>D: ROLLBACK
        S-->>V: 404 또는 403
    else 다른 사용자가 먼저 수정
        S->>D: ROLLBACK
        S-->>V: 409 VERSION_CONFLICT
        V-->>U: 초안 유지·최신 내용 비교
    else 변경 가능
        S->>D: 업무 수정·version 증가·COMMIT
        S-->>V: 200 갱신된 업무
        V->>R: 목록·보드·집계 재조회
        V-->>U: 최신 상태 표시
    end
```

인증 조회 Session은 짧게 종료하고 서비스는 별도 Session의 트랜잭션을 사용한다. 인증 실패는 서비스 실행 전에 401로 끝난다. 위 순서도는 유효한 인증을 통과한 업무 변경의 주요 분기다.

## 3. 핵심 데이터 관계

```mermaid
erDiagram
    USERS ||--o{ AUTH_SESSIONS : "로그인 세션"
    USERS ||--o{ PROJECTS : "소유"
    USERS ||--o{ PROJECT_MEMBERSHIPS : "참여"
    PROJECTS ||--|{ PROJECT_MEMBERSHIPS : "팀원"
    PROJECTS ||--o{ TASKS : "업무"
    USERS ||--o{ TASKS : "작성"
    PROJECT_MEMBERSHIPS o|--o{ TASKS : "담당"

    USERS {
        uuid id PK
        string email UK
        string display_name
        string password_hash
    }
    AUTH_SESSIONS {
        uuid id PK
        string token_hash UK
        uuid user_id FK
        datetime expires_at
    }
    PROJECTS {
        uuid id PK
        uuid owner_user_id FK
        string name
        date start_date
        date end_date
        int version
    }
    PROJECT_MEMBERSHIPS {
        uuid project_id PK,FK
        uuid user_id PK,FK
        datetime joined_at
    }
    TASKS {
        uuid id PK
        uuid project_id FK
        uuid assignee_user_id FK
        uuid created_by_user_id FK
        string title
        string status
        date due_date
        int version
    }
```

가독성을 위해 감사 시각·설명 등 일부 열을 생략했다. 상세 타입·null·인덱스는 설계 본문을 따른다. 담당자 연결은 `(project_id, assignee_user_id)` 복합 FK다. 소유자는 소속 멤버여야 하며 지연 검사되는 소유자 멤버십 FK로 보장한다.

## 4. 화면 이동 흐름

```mermaid
flowchart TB
    login["S01 로그인"]
    register["S02 회원가입"]
    projects["S03 내 프로젝트"]
    create["S04 프로젝트 만들기"]
    tasks["S05 업무<br/>목록 ↔ 상태 보드"]
    detail["S06 업무 상세"]
    members["S07 팀원"]
    settings["S08 프로젝트 설정<br/>소유자만"]
    missing["S09 접근 불가 / 없는 페이지"]
    taskform["업무 생성·수정 폼"]
    conflict["수정 충돌<br/>최신 내용과 초안 비교"]

    login -->|"계정 만들기"| register
    register -->|"가입 완료"| login
    login -->|"로그인 성공"| projects
    projects -->|"새 프로젝트"| create
    create -->|"생성 완료"| tasks
    projects -->|"프로젝트 선택"| tasks
    tasks <-->|"목록과 상세"| detail
    tasks <-->|"프로젝트 내 탭"| members
    tasks <-->|"소유자 설정"| settings
    tasks -->|"새 업무"| taskform
    detail -->|"수정"| taskform
    taskform -->|"저장 성공"| tasks
    taskform -->|"버전 충돌"| conflict
    conflict -->|"비교 후 다시 편집"| taskform
    tasks -->|"소속 해제·프로젝트 삭제"| missing
    missing -->|"내 프로젝트로"| projects
    settings -->|"프로젝트 삭제 완료"| projects
```

상세 화면에서 편집을 열었다면 성공 후 상세에 머물고, 목록·보드에서 새 업무를 만들었다면 원래 화면을 갱신한다. 그림의 ‘저장 성공’ 화살표는 업무 영역으로 돌아오는 흐름을 요약한다. 세션이 만료되면 어느 보호 화면에서든 로그인으로 이동한다.

## 5. 화면 시안에서 확인할 항목

- 로그인·회원가입, 프로젝트 목록·생성, 업무 목록·보드·상세, 팀원, 설정, 접근 불가 화면.
- 소유자와 팀원의 메뉴·관리 버튼 차이.
- 업무 생성·수정, 삭제 확인, 팀원 추가·제거, 소유권 이전 대화상자.
- 빈 데이터, 조회 실패, 수정 충돌 화면 상태.
- 데스크톱과 좁은 화면의 정보 배치.

대화 안의 화면 시안은 예시 데이터로 동작하는 검토용 프로토타입이다. 실제 계정 생성·로그인·DB 저장은 수행하지 않는다. 모바일 미리보기의 보드 열은 시안의 가독성을 위해 세로로 배치했으며, 제품의 모바일 보드 이동 방식은 화면 명세의 가로 스크롤과 비교해 검토할 수 있다.
