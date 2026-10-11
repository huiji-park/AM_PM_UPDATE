# AM_PM MVP — API 계약서

- 작성일: 2026-10-03
- 버전: 0.2 / 제약 검토 보완, 사용자 검토용
- 상위 문서: [상세 설계](2026-10-03-am-pm-mvp-design.md)
- 관련 문서: [화면·검증 명세](2026-10-03-am-pm-mvp-screens-verification.md)

이 문서의 URL·JSON은 구현할 계약 예시다. 현재 실행되는 API나 자동 생성된 OpenAPI가 아니다. 구현 시 OpenAPI와 이 계약의 일치 여부를 검증한다.

## 1. 공통 계약

### 1.1 경로·표현

- 업무 API prefix는 `/api/v1`이다. health는 `/health/live`, `/health/ready`다.
- 요청·응답 필드는 `snake_case`, 상태 값은 `TODO`, `IN_PROGRESS`, `DONE`이다.
- ID는 UUID 문자열, 감사 시각은 UTC ISO 8601, 일정 날짜는 `YYYY-MM-DD`다.
- 단일 성공 응답은 리소스 객체 자체다. `success: true`나 중복 `data` 래퍼를 붙이지 않는다.
- 목록은 `items`, `page`, `page_size`, `total`, `total_pages`를 가진다.
- 생성은 201, 조회·수정·명령은 200, 본문 없는 삭제·로그아웃·탈퇴는 204다.
- 인증된 API는 쿠키를 사용한다. Authorization Bearer 토큰은 이 버전의 계약이 아니다.
- 모든 응답은 `X-Request-ID`를 포함한다. 보호 데이터와 인증 응답은 `Cache-Control: no-store`다.
- 서버가 계산하는 필드(`id`, `role`, `created_by`, `version` 등)를 요청에 넣으면 422다.
- 존재하지 않는 경로는 404, 지원하지 않는 메서드는 405다. 가능한 앱 오류는 공통 오류 envelope로 변환한다.

### 1.2 상태 변경 요청 헤더

```http
Origin: http://localhost:5173
X-AMPM-Request: 1
Content-Type: application/json
```

실제 배포에서는 Origin 값이 배포 PUBLIC_ORIGIN이다. POST/PATCH/DELETE 모두 Origin과 전용 헤더가 필요하다. DELETE·로그아웃처럼 본문이 없는 요청에는 Content-Type을 요구하지 않는다. JSON이 필요한 요청의 잘못된 미디어 타입은 415, 문법이 깨진 JSON은 422다. GET은 전용 헤더를 요구하지 않는다.

프록시·API는 원문 본문 262144바이트까지 허용하고 초과하면 413이다. Content-Encoding은 미지정 또는 identity만 허용하며 그 외 값은 415다. 본문 크기는 문자 수 제한과 별도로 적용한다. 개발용 Swagger와 OpenAPI는 `/api/docs`, `/api/openapi.json`이며 아래 21개 업무·health 엔드포인트 수에는 포함하지 않는다. production에서는 이 문서 경로를 제공하지 않는다.

Origin 검사 실패는 업무 권한 검사보다 먼저 403 `UNTRUSTED_REQUEST`로 반환한다. 이후 정상 형식의 보호 요청에서 인증 실패는 401, 프로젝트 미소속은 404, 소속자의 역할 부족은 403이다. 잘못된 UUID·입력 형식은 422가 될 수 있으며, 형식 오류 응답에 리소스 존재 정보를 담지 않는다.

### 1.3 입력 정규화·PATCH

| 입력 | 처리 |
|---|---|
| 이메일 | 문법 검증, trim, 정규화, 전체 소문자화 |
| 프로젝트 이름·업무 제목·표시 이름 | trim 후 길이 검사 |
| 설명 | 빈 문자열 허용, 줄바꿈 보존, CRLF를 LF로 통일, null 불허 |
| 비밀번호 | 입력 그대로 처리, trim·정규화 없음 |
| 날짜 | 실제 유효한 날짜만 허용 |
| 페이지·버전 | JSON version은 정수이며 bool 불허; query는 정수 문자열만 허용 |

PATCH에서 생략한 필드는 현재 값 유지다. `null`은 nullable 필드의 값 제거다. nullable이 아닌 필드에 null은 422다. 부분 입력을 기존 리소스와 합친 최종 상태에 대해 기간·담당자 규칙을 검증한다. 예를 들어 `start_date`가 남아 있는 상태에서 `due_date: null`만 보내면 422다.

프로젝트 기간을 비우려면 `start_date: null`, `end_date: null`을 같이 보낸다. 업무 시작일만 비우고 마감일을 유지하는 것은 가능하다. 실제 수정 필드 없이 expected_version만 제출하면 422 `VALIDATION_ERROR`다.

### 1.4 페이지·검색·정렬

`page` 기본 1·최소 1, `page_size` 기본 20·범위 1~100이다. 마지막 페이지를 초과하면 items는 빈 배열이며 total은 실제 값이다. total=0이면 total_pages=0이다. 범위 밖 값을 임의 보정하지 않고 422를 반환한다.

`q`는 trim 후 최대 100자이고 빈 문자열이면 필터를 생략한다. 프로젝트는 이름, 업무는 제목을 대소문자 구분 없이 부분 검색한다. `%`, `_`, 역슬래시는 검색 문법이 아니라 리터럴 문자로 escape한다. 설명은 검색하지 않는다.

| 대상 | sort 값 | 정렬·동률 처리 |
|---|---|---|
| 프로젝트 | `-created_at` 기본 | created_at DESC, id DESC |
| 프로젝트 | `name` | name ASC, id ASC; DB 기본 collation |
| 업무 | `-created_at` 기본 | created_at DESC, id DESC |
| 업무 | `-updated_at` | updated_at DESC, id DESC |
| 업무 | `due_date` | due_date ASC NULLS LAST, id ASC |
| 팀원 | 선택 불가 | 소유자 먼저, joined_at ASC, user_id ASC |

정렬 필드·상태 값·필터 query의 알 수 없는 키는 422다. query의 단일 필드를 두 번 전달하는 것도 422로 거부하여 해석 차이를 피한다. 업무 보드의 세 열은 각각 status를 달리한 같은 목록 API를 사용한다.

### 1.5 버전 계약

기존 리소스 변경에 제출하는 expected_version은 양의 정수다. 프로젝트 설정·멤버십·소유권 변경은 Project의 version, 업무 수정·삭제는 Task의 version을 사용한다. GET 응답의 version은 수정 충돌용이며 전체 JSON 표현의 HTTP ETag가 아니다.

409 VERSION_CONFLICT일 때 서버는 현재 version만 반환한다. 프론트가 최신 리소스를 GET하여 사용자에게 다시 비교하게 한다. 409에 맞춰 version만 바꾸어 자동 재요청하지 않는다.

## 2. 공통 응답 스키마

### 2.1 User와 UserSummary

인증 API만 이메일을 포함한 User를 반환한다.

```json
{
  "id": "10000000-0000-4000-8000-000000000001",
  "email": "alice@example.test",
  "display_name": "앨리스",
  "created_at": "2026-10-03T12:00:00Z"
}
```

프로젝트·업무에서 참조하는 UserSummary는 `id`, `display_name` 두 필드만 가진다. 다른 팀원의 이메일·비밀번호 해시·세션 정보는 포함하지 않는다.

### 2.2 ProjectSummary·ProjectDetail

ProjectSummary 필드: `id`, `name`, `description`, `start_date`, `end_date`, `owner`, `my_role`, `version`, `created_at`, `updated_at`.

`owner`는 UserSummary, `my_role`은 `OWNER | MEMBER`다. 기간은 nullable이다. ProjectDetail은 여기에 `member_count`, `task_counts`, `progress_percent`를 추가한다.

```json
{
  "id": "20000000-0000-4000-8000-000000000001",
  "name": "AM_PM 재구현",
  "description": "팀 프로젝트 관리 서비스 만들기",
  "start_date": "2026-10-03",
  "end_date": "2026-11-30",
  "owner": {
    "id": "10000000-0000-4000-8000-000000000001",
    "display_name": "앨리스"
  },
  "my_role": "OWNER",
  "version": 1,
  "member_count": 1,
  "task_counts": {"total": 0, "todo": 0, "in_progress": 0, "done": 0},
  "progress_percent": 0,
  "created_at": "2026-10-03T12:30:00Z",
  "updated_at": "2026-10-03T12:30:00Z"
}
```

집계는 화면 필터와 무관한 프로젝트 전체 업무 기준이다. `progress_percent = floor(done / total * 100)`이며 total=0이면 0이다. task_counts.total은 세 상태의 합이다. 소속 검증과 집계는 동일 조회 문장의 snapshot에 속하도록 구성한다. 업무 생성·수정으로 집계가 변해도 project.version은 바뀌지 않는다.

### 2.3 Member

```json
{
  "user": {
    "id": "10000000-0000-4000-8000-000000000002",
    "display_name": "보라"
  },
  "role": "MEMBER",
  "joined_at": "2026-10-03T12:40:00Z"
}
```

role은 프로젝트 owner_user_id에서 계산한다. 프로젝트당 OWNER는 정확히 하나다.

### 2.4 Task

```json
{
  "id": "30000000-0000-4000-8000-000000000001",
  "project_id": "20000000-0000-4000-8000-000000000001",
  "title": "프로젝트 목록 화면 구현",
  "description": "로딩·빈 화면·조회 실패 상태 포함",
  "status": "TODO",
  "assignee": {
    "id": "10000000-0000-4000-8000-000000000002",
    "display_name": "보라"
  },
  "created_by": {
    "id": "10000000-0000-4000-8000-000000000001",
    "display_name": "앨리스"
  },
  "start_date": "2026-10-04",
  "due_date": "2026-10-10",
  "completed_at": null,
  "is_overdue": false,
  "version": 1,
  "created_at": "2026-10-03T12:45:00Z",
  "updated_at": "2026-10-03T12:45:00Z"
}
```

assignee는 UserSummary 또는 null이다. 요청의 assignee_user_id를 응답에서도 중복 제공하지 않는다. 목록·상세의 Task 필드 계약은 같으며 목록은 각 행을 추가 조회하지 않도록 사용자 요약을 함께 로드한다.

### 2.5 목록 envelope

```json
{
  "items": [],
  "page": 1,
  "page_size": 20,
  "total": 0,
  "total_pages": 0
}
```

items의 타입만 ProjectSummary, Member, Task로 달라진다. total과 items는 같은 필터·동일 DB snapshot을 기준으로 계산한다. 여러 SELECT로 구성하는 목록 조회는 첫 쿼리 이전부터 읽기 전용 REPEATABLE READ로 실행하고 소속 검사·count·rows를 같은 트랜잭션에 넣는다. 단일 SELECT로 동일 결과를 구성하는 구현도 허용한다. 서로 다른 HTTP 요청 사이의 snapshot 고정은 제공하지 않으므로 보드 세 열이나 페이지 이동 전체의 순간적 일관성은 보장하지 않는다.

## 3. 전체 엔드포인트

표의 경로는 health를 제외하고 `/api/v1` 뒤에 붙인다. ‘소속’은 프로젝트 멤버십 검사, ‘소유’는 소속 검사 후 소유권 검사를 뜻한다.

| ID | 메서드·경로 | 권한 | 성공 | 입력 → 출력 |
|---|---|---|---|---|
| A01 | POST /auth/register | 공개 | 201 | Register → User |
| A02 | POST /auth/login | 공개 | 200 | Login → User + 쿠키 |
| A03 | POST /auth/logout | 세션 유무 무관 | 204 | 본문 없음 → 쿠키 삭제 |
| A04 | GET /auth/me | 로그인 | 200 | 없음 → User |
| P01 | GET /projects | 로그인 | 200 | query → ProjectSummary 목록 |
| P02 | POST /projects | 로그인 | 201 | ProjectCreate → ProjectDetail |
| P03 | GET /projects/{project_id} | 소속 | 200 | 없음 → ProjectDetail |
| P04 | PATCH /projects/{project_id} | 소유 | 200 | ProjectPatch → ProjectDetail |
| P05 | DELETE /projects/{project_id} | 소유 | 204 | query version → 없음 |
| M01 | GET /projects/{project_id}/members | 소속 | 200 | page query → Member 목록 |
| M02 | POST /projects/{project_id}/members | 소유 | 201 | MemberAdd → Member + project_version |
| M03 | DELETE /projects/{project_id}/members/{user_id} | 소유 | 204 | query version → 없음 |
| M04 | POST /projects/{project_id}/leave | 소속 | 204 | VersionCommand → 없음 |
| M05 | POST /projects/{project_id}/transfer-ownership | 소유 | 200 | Transfer → ProjectDetail |
| T01 | GET /projects/{project_id}/tasks | 소속 | 200 | query → Task 목록 |
| T02 | POST /projects/{project_id}/tasks | 소속 | 201 | TaskCreate → Task |
| T03 | GET /projects/{project_id}/tasks/{task_id} | 소속 | 200 | 없음 → Task |
| T04 | PATCH /projects/{project_id}/tasks/{task_id} | 소속 | 200 | TaskPatch → Task |
| T05 | DELETE /projects/{project_id}/tasks/{task_id} | 소유 또는 업무 작성자 | 204 | query version → 없음 |
| H01 | GET /health/live | 공개 | 200 | 없음 → Health |
| H02 | GET /health/ready | 공개 | 200/503 | 없음 → Health/공통 오류 |

## 4. 인증 API

### A01 회원가입

```http
POST /api/v1/auth/register
```

```json
{
  "email": "alice@example.test",
  "display_name": "앨리스",
  "password": "ExamplePass-2026!"
}
```

이메일은 최대 254자, display_name 1~50자, password 12~128 코드 포인트다. 비밀번호 확인은 프론트에서만 검사하며 `password_confirm`을 서버에 보내지 않는다. 요청은 로그인 여부와 무관하게 같은 계약이다. 가입으로 기존 로그인 세션을 변경하지 않는다. 성공은 User, 자동 로그인은 하지 않는다.

오류: 422 VALIDATION_ERROR, 409 EMAIL_ALREADY_REGISTERED, 429 RATE_LIMITED. 가입 성공 후 프론트는 이메일만 로그인 화면에 전달하며 비밀번호는 지운다.

### A02 로그인

```json
{"email": "alice@example.test", "password": "ExamplePass-2026!"}
```

성공: 200 User와 Set-Cookie. 실패: 401 INVALID_CREDENTIALS. 로그인 입력도 동일 이메일·비밀번호 형식 제한을 적용한다. 실패 응답에는 계정 존재 여부를 구분하는 코드를 넣지 않는다. DB 저장이 실패했으면 세션 쿠키를 발급하지 않는다.

### A03 로그아웃

본문 없이 POST한다. 쿠키가 없거나 이미 만료·삭제된 경우에도 204와 만료 쿠키를 반환한다. 정상 세션이면 해당 세션만 삭제한다. DB 장애로 삭제 성공 여부를 보장할 수 없으면 503이며 성공으로 표시하지 않는다. Origin·전용 헤더 검사는 항상 적용한다.

### A04 현재 사용자

세션 쿠키가 유효하면 User를 반환한다. 누락·변조·만료·삭제는 모두 401 AUTHENTICATION_REQUIRED이며 남은 쿠키를 지운다. 앱 초기화 중 5xx나 통신 실패는 비로그인 상태로 바꾸지 않는다.

## 5. 프로젝트 API

### P01 목록

```http
GET /api/v1/projects?q=AM_PM&sort=-created_at&page=1&page_size=20
```

인증된 사용자의 멤버십이 있는 프로젝트만 반환한다. 소유자 여부와 무관하다. q, sort, page, page_size 외 query는 허용하지 않는다. 검색어가 없고 프로젝트가 0개인 경우와 검색 결과가 0개인 경우의 차이는 프론트가 q 유무로 판단한다.

### P02 생성

```json
{
  "name": "AM_PM 재구현",
  "description": "팀 프로젝트 관리 서비스 만들기",
  "start_date": "2026-10-03",
  "end_date": "2026-11-30"
}
```

name은 필수, description 기본 `""`, 두 날짜 기본 null이다. owner_user_id·members는 받지 않는다. 생성자가 자동 소유자가 된다. 성공 시 ProjectDetail과 `Location: /api/v1/projects/{id}`를 반환한다.

### P03 상세

ProjectDetail을 반환한다. 집계는 전체 업무 기준이고 현재 업무 검색어·상태 필터와 무관하다. 비소속·없는 프로젝트는 같은 404 PROJECT_NOT_FOUND다.

### P04 수정

```json
{"name": "AM_PM MVP", "expected_version": 1}
```

수정 가능 필드: name, description, start_date, end_date. expected_version은 필수다. 수정 성공 시 갱신된 ProjectDetail을 반환한다. 소유권 변경은 이 API로 할 수 없다.

### P05 삭제

```http
DELETE /api/v1/projects/20000000-0000-4000-8000-000000000001?expected_version=3
```

소유자·버전 확인 후 업무·멤버십·프로젝트를 함께 삭제한다. 다른 팀원이 있어도 허용한다. 재호출 시 존재하지 않으므로 404이며 204로 위장하지 않는다.

## 6. 팀원·소유권 API

### M01 목록

page, page_size만 받는다. 일반 팀원도 모든 팀원의 UserSummary·역할을 볼 수 있다. 프론트 담당자 선택창은 이 API의 페이지를 불러오며, 첫 100명만 전체라고 간주하지 않는다. ‘더 보기’로 다음 페이지를 추가하고 선택한 사용자 요약을 유지한다.

### M02 추가

```json
{"email": "bora@example.test", "expected_version": 1}
```

권한·project.version 확인 후 정확한 이메일로 사용자 조회, 중복 검사, 멤버십 생성, project.version +1을 한 트랜잭션에서 처리한다.

```json
{
  "member": {
    "user": {"id": "10000000-0000-4000-8000-000000000002", "display_name": "보라"},
    "role": "MEMBER",
    "joined_at": "2026-10-03T12:40:00Z"
  },
  "project_version": 2
}
```

404 USER_NOT_FOUND는 권한이 확인된 소유자에게만 제공한다. 409 MEMBER_ALREADY_EXISTS는 중복 멤버다. 같은 요청이 다른 탭에서 먼저 처리되어 버전도 바뀌었다면 VERSION_CONFLICT가 먼저다. 이메일이 동일한 원본 계정의 과거 데이터를 자동 연결하지 않는다.

### M03 제거

```http
DELETE /api/v1/projects/{project_id}/members/{user_id}?expected_version=2
```

소유자가 일반 팀원만 제거할 수 있다. 소유자 본인을 대상으로 하면 409 OWNER_CANNOT_LEAVE, 없는 멤버면 404 MEMBER_NOT_FOUND다. 담당 업무는 삭제하지 않고 assignee를 null로 만든다. 업무별 버전과 프로젝트 버전이 증가한다. 성공 후 프론트는 프로젝트 상세·멤버·업무를 재조회한다.

### M04 본인 탈퇴

```json
{"expected_version": 2}
```

URL이나 body로 대상 user_id를 받지 않는다. 현재 인증 사용자만 탈퇴시킨다. 소유자는 409 OWNER_CANNOT_LEAVE다. 성공 시 담당 해제·멤버십 삭제·프로젝트 버전 증가를 커밋하고 204다. 프론트는 프로젝트 목록으로 이동한다. 탈퇴 후 같은 프로젝트 조회는 404다.

### M05 소유권 이전

```json
{
  "new_owner_user_id": "10000000-0000-4000-8000-000000000002",
  "expected_version": 2
}
```

대상이 현재 프로젝트 멤버가 아니면 422 OWNER_MUST_BE_MEMBER, 현재 소유자 자신이면 409 OWNER_UNCHANGED다. 소유자 ID 변경·version 증가를 함께 커밋한다. 이전 소유자의 멤버십을 삭제하지 않는다.

응답은 요청자 기준 my_role이 MEMBER인 ProjectDetail이다. 새 소유자의 다음 요청부터 소유자 권한이 적용된다. 모든 쓰기에서 잠금 후 권한을 재검증하므로 이전 소유자의 대기 중인 소유자 전용 요청도 최신 권한으로 판단한다.

## 7. 업무 API

### T01 목록·필터

```http
GET /api/v1/projects/{project_id}/tasks?status=IN_PROGRESS&assignee=me&sort=due_date&page=1&page_size=20
```

| query | 허용 값 | 의미 |
|---|---|---|
| q | 최대 100자 | 제목 부분 검색 |
| status | TODO / IN_PROGRESS / DONE | 생략하면 모든 상태 |
| assignee | `me`, `unassigned`, UUID | 나·미배정·특정 사용자; 생략 시 전체 |
| due_from | 날짜 | 마감일 ≥ 해당 날짜 |
| due_to | 날짜 | 마감일 ≤ 해당 날짜 |
| overdue | `true`, `false` | 참이면 지연, 거짓이면 지연이 아닌 업무만; 생략 시 전체 |
| sort | -created_at / -updated_at / due_date | 공통 정렬 계약 |
| page, page_size | 정수 | 공통 페이지 계약 |

모든 필터는 AND다. due_from > due_to이면 422다. 마감 범위 필터가 있으면 마감일 null인 업무는 제외한다. overdue=false에는 완료 업무와 마감일 없는 업무도 포함한다. DONE+overdue=true는 200 빈 목록이다.

형식이 올바른 assignee UUID가 비소속 사용자여도 필터 결과는 빈 목록이다. 이 GET 필터로 사용자 존재 여부를 확인하지 않는다. 생성·수정에서는 비소속 담당자를 422로 거부한다.

같은 요청의 items와 total은 동일한 `today` 기준을 사용한다. 지연 기준 날짜는 요청 처리 시작 시 한 번 Asia/Seoul로 계산해 쿼리에 전달한다. 자정 경계를 건너도 한 응답 안에서 기준이 달라지지 않는다.

### T02 생성

```json
{
  "title": "프로젝트 목록 화면 구현",
  "description": "로딩·빈 화면·조회 실패 상태 포함",
  "assignee_user_id": "10000000-0000-4000-8000-000000000002",
  "start_date": "2026-10-04",
  "due_date": "2026-10-10"
}
```

title 필수, description 기본 빈 문자열, status 기본 TODO, 나머지 기본 null이다. status를 명시할 수도 있으며 DONE으로 만들면 completed_at을 생성 시각으로 설정한다. project_id와 created_by는 URL·현재 사용자에서 정한다. expected_version은 받지 않는다.

프로젝트 잠금 후 멤버십·담당자 확인, 업무 생성, commit한다. 성공은 Task와 Location이다. 프로젝트 기간 밖의 일정은 오류가 아니다.

### T03 상세

project_id와 task_id를 함께 조건으로 조회한다. 해당 프로젝트에 소속돼도 다른 프로젝트의 task_id는 조회할 수 없다. 없는 업무는 404 TASK_NOT_FOUND다.

### T04 수정

```json
{"status": "DONE", "expected_version": 1}
```

수정 가능 필드: title, description, status, assignee_user_id, start_date, due_date. expected_version 필수다. 일반 팀원도 다른 사람이 작성한 업무를 수정할 수 있다. created_by, project_id, completed_at을 바꿀 수 없다.

담당자를 제거하려면 assignee_user_id=null이다. 비소속 담당자 지정은 422 ASSIGNEE_NOT_MEMBER다. 두 사람이 같은 version을 수정하면 처음 커밋한 요청만 성공하고 나머지는 409다. 응답은 최신 Task다.

### T05 삭제

```http
DELETE /api/v1/projects/{project_id}/tasks/{task_id}?expected_version=2
```

현재 소속을 먼저 검사한 후 소유자 또는 작성자인지 확인한다. 업무 담당자라는 이유만으로 삭제할 수 없다. 역할 부족은 403 FORBIDDEN, 버전 불일치는 409다. 성공은 204다.

## 8. 오류 계약

### 8.1 envelope

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력값을 확인해 주세요.",
    "details": [{"field": "due_date", "message": "마감일은 시작일보다 빠를 수 없습니다."}],
    "context": {}
  },
  "request_id": "40000000-0000-4000-8000-000000000001"
}
```

code는 프론트 분기용 고정 문자열, message는 한국어 사용자 문구다. details는 항상 배열이며 필드 오류가 없으면 `[]`다. field는 JSON/query 필드명이고 전체 입력 오류는 `_form`이다. context는 항상 객체다. Pydantic 원본 오류의 `input`과 예외 stack은 응답에 포함하지 않는다.

```json
{
  "error": {
    "code": "VERSION_CONFLICT",
    "message": "다른 사용자가 먼저 수정했습니다. 최신 내용을 확인해 주세요.",
    "details": [],
    "context": {"current_version": 4}
  },
  "request_id": "40000000-0000-4000-8000-000000000002"
}
```

### 8.2 오류 코드표

| HTTP | code | 의미·프론트 동작 |
|---|---|---|
| 401 | AUTHENTICATION_REQUIRED | 보호 화면·입력 초안 지우고 로그인 이동 |
| 401 | INVALID_CREDENTIALS | 로그인 폼 공통 오류, 비밀번호 지우기 |
| 403 | UNTRUSTED_REQUEST | 잘못된 Origin/전용 헤더, 환경 오류 안내 |
| 403 | FORBIDDEN | 역할 부족, 편집 차단·권한 재조회 |
| 404 | PROJECT_NOT_FOUND | 접근할 수 없거나 없는 프로젝트, 목록 이동 링크 |
| 404 | TASK_NOT_FOUND | 업무 삭제·잘못된 소속, 업무 목록 이동 링크 |
| 404 | USER_NOT_FOUND | 팀원 추가 대상 없음, 이메일 필드 안내 |
| 404 | MEMBER_NOT_FOUND | 이미 제거된 멤버, 목록 재조회 |
| 404 | NOT_FOUND | 등록되지 않은 API 경로 |
| 405 | METHOD_NOT_ALLOWED | 지원하지 않는 메서드 |
| 409 | EMAIL_ALREADY_REGISTERED | 중복 이메일, 가입 폼 필드 오류 |
| 409 | MEMBER_ALREADY_EXISTS | 이미 소속된 사용자 |
| 409 | OWNER_CANNOT_LEAVE | 먼저 소유권 이전 필요 |
| 409 | OWNER_UNCHANGED | 현재 소유자로 이전 요청 |
| 409 | VERSION_CONFLICT | 초안 유지, 최신 데이터와 비교 |
| 413 | PAYLOAD_TOO_LARGE | 프록시 본문 상한 초과 |
| 415 | UNSUPPORTED_MEDIA_TYPE | JSON이 필요한 경로의 미디어 타입 오류 |
| 422 | VALIDATION_ERROR | 형식·길이·날짜·알 수 없는 필드 |
| 422 | ASSIGNEE_NOT_MEMBER | 담당자가 현재 프로젝트에 없음 |
| 422 | OWNER_MUST_BE_MEMBER | 새 소유자가 현재 프로젝트에 없음 |
| 429 | RATE_LIMITED | 인증 요청 제한, 재시도 안내 |
| 500 | INTERNAL_ERROR | 일반 오류와 request_id만 표시 |
| 503 | RESOURCE_BUSY | 잠금·경쟁·DB pool 획득 실패, 자동 쓰기 재시도 안 함 |
| 503 | SERVICE_UNAVAILABLE | DB 장애·statement timeout·readiness 실패 |

알려진 도메인 오류는 코드·status를 일관되게 반환한다. 프록시가 생성한 413·429·502·504는 JSON envelope를 보장하지 않으므로 HTTP status와 일반 fallback 문구를 사용한다. 429에 Retry-After가 있으면 표시하고, 없으면 ‘잠시 후 다시 시도’라고 안내한다.

### 8.3 업무 검증 순서

정상 형식의 쓰기 요청은 프로젝트 잠금 → 소속 확인 → 역할 확인 → 대상 리소스 확인 → 버전 비교 → 최종 입력·관계 규칙 검사 → DB 변경 → commit 순서다. 업무 삭제 권한은 대상의 작성자를 알아야 하므로 대상 조회 후 확인한다.

팀원 추가의 사용자 조회는 소유자 검사 뒤다. 비소속 사용자가 이메일이나 task_id를 추측해도 해당 리소스의 정보를 응답하지 않는다. 존재하지 않는 프로젝트 잠금을 얻을 수 없으면 즉시 404다. 프로젝트 생성에는 기존 행 잠금이 필요 없다.

## 9. health 계약

live 성공: `{"status":"ok"}`. ready 성공: `{"status":"ready"}`. ready 실패: 503 SERVICE_UNAVAILABLE envelope. 연결 문자열, DB 이름, 마이그레이션 파일 경로는 외부 응답에 포함하지 않는다.

## 10. API 계약 검증 기준

1. 위 21개 엔드포인트가 OpenAPI에 있고 request/response 필드·상태 코드가 일치한다.
2. 모든 프로젝트·업무 엔드포인트에 계정 A/B/C 권한 테스트가 있다.
3. Cookie 인증·Origin·전용 헤더·입력 거부 규칙을 브라우저와 API 통합 테스트에서 확인한다.
4. 정상 응답에 password_hash·token_hash·원본 세션 토큰이 없는지 검사한다.
5. 예시 JSON은 독립적으로 파싱 가능하며 생성·수정 스키마와 맞는다.
6. 명시하지 않은 API 호환 레이어로 기존 Spring 응답을 흉내 내지 않는다. 신규 Vue 화면이 이 계약을 사용한다.
