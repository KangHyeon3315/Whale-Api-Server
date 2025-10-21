# 📧 Email Management API Documentation

## 📋 목차
- [개요](#개요)
- [인증](#인증)
- [이메일 계정 관리 API](#이메일-계정-관리-api)
- [이메일 조회 API](#이메일-조회-api)
- [첨부파일 관리 API](#첨부파일-관리-api)
- [동기화 API](#동기화-api)
- [Gmail OAuth API](#gmail-oauth-api)
- [에러 코드](#에러-코드)

## 개요

Whale API Server의 이메일 관리 시스템은 다음과 같은 기능을 제공합니다:
- **1개 Naver Mail + 2개 Gmail 계정** 통합 관리
- **실시간 이메일 동기화** 및 조회
- **첨부파일 다운로드** 및 관리
- **OAuth2 기반 Gmail 인증** 및 **IMAP 기반 Naver 인증**

### Base URL
```
https://api.whale.com/v1
```

### Content-Type
```
application/json
```

## 인증

모든 API는 `@RequireAuth` 어노테이션을 통해 인증이 필요합니다.

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

## 이메일 계정 관리 API

### 1. 이메일 계정 등록

**POST** `/email/accounts/register`

이메일 계정을 시스템에 등록합니다.

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "emailAddress": "user@gmail.com",
  "provider": "GMAIL",
  "password": "encrypted_password",
  "displayName": "My Gmail Account",
  "syncEnabled": true,
  "gmailAuthCode": "4/0AX4XfWh..." // Gmail인 경우 필수
}
```

**Response:**
```json
{
  "identifier": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "emailAddress": "user@gmail.com",
  "provider": "GMAIL",
  "displayName": "My Gmail Account",
  "isActive": true,
  "syncEnabled": true,
  "lastSyncDate": null,
  "createdDate": "2024-01-01T00:00:00Z",
  "modifiedDate": "2024-01-01T00:00:00Z"
}
```

### 2. 사용자 이메일 계정 목록 조회

**GET** `/email/accounts?userId={userId}`

사용자의 모든 이메일 계정을 조회합니다.

**Query Parameters:**
- `userId` (required): 사용자 ID

**Response:**
```json
[
  {
    "identifier": "550e8400-e29b-41d4-a716-446655440001",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "emailAddress": "user@gmail.com",
    "provider": "GMAIL",
    "displayName": "My Gmail Account",
    "isActive": true,
    "syncEnabled": true,
    "lastSyncDate": "2024-01-01T12:00:00Z",
    "createdDate": "2024-01-01T00:00:00Z",
    "modifiedDate": "2024-01-01T00:00:00Z"
  }
]
```

### 3. 특정 이메일 계정 조회

**GET** `/email/accounts/{accountId}?userId={userId}`

특정 이메일 계정의 상세 정보를 조회합니다.

**Path Parameters:**
- `accountId`: 이메일 계정 ID

**Query Parameters:**
- `userId` (required): 사용자 ID

**Response:**
```json
{
  "identifier": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "emailAddress": "user@gmail.com",
  "provider": "GMAIL",
  "displayName": "My Gmail Account",
  "isActive": true,
  "syncEnabled": true,
  "lastSyncDate": "2024-01-01T12:00:00Z",
  "createdDate": "2024-01-01T00:00:00Z",
  "modifiedDate": "2024-01-01T00:00:00Z"
}
```

## 이메일 조회 API

### 1. 이메일 목록 조회

**GET** `/email/accounts/{accountId}/emails`

특정 계정의 이메일 목록을 조회합니다.

**Path Parameters:**
- `accountId`: 이메일 계정 ID

**Query Parameters:**
- `userId` (required): 사용자 ID
- `folderName` (optional): 폴더명 (INBOX, SENT, DRAFT 등)
- `isRead` (optional): 읽음 상태 필터
- `limit` (optional, default: 20): 페이지 크기
- `offset` (optional, default: 0): 페이지 오프셋

**Response:**
```json
{
  "emails": [
    {
      "identifier": "550e8400-e29b-41d4-a716-446655440002",
      "emailAccountIdentifier": "550e8400-e29b-41d4-a716-446655440001",
      "messageId": "CADuG1B7...",
      "subject": "Welcome to Whale API",
      "fromAddress": "noreply@whale.com",
      "fromName": "Whale Team",
      "toAddresses": ["user@gmail.com"],
      "ccAddresses": [],
      "bccAddresses": [],
      "receivedDate": "2024-01-01T10:00:00Z",
      "sentDate": "2024-01-01T09:59:00Z",
      "isRead": false,
      "isImportant": false,
      "folderName": "INBOX",
      "hasAttachments": true,
      "bodyPreview": "Welcome to Whale API Server...",
      "createdDate": "2024-01-01T10:01:00Z",
      "modifiedDate": "2024-01-01T10:01:00Z"
    }
  ],
  "totalCount": 150,
  "hasNext": true,
  "hasPrevious": false
}
```

### 2. 특정 이메일 상세 조회

**GET** `/email/{emailId}`

특정 이메일의 상세 정보를 조회합니다.

**Path Parameters:**
- `emailId`: 이메일 ID

**Query Parameters:**
- `userId` (required): 사용자 ID

**Response:**
```json
{
  "identifier": "550e8400-e29b-41d4-a716-446655440002",
  "emailAccountIdentifier": "550e8400-e29b-41d4-a716-446655440001",
  "messageId": "CADuG1B7...",
  "subject": "Welcome to Whale API",
  "fromAddress": "noreply@whale.com",
  "fromName": "Whale Team",
  "toAddresses": ["user@gmail.com"],
  "ccAddresses": [],
  "bccAddresses": [],
  "receivedDate": "2024-01-01T10:00:00Z",
  "sentDate": "2024-01-01T09:59:00Z",
  "isRead": false,
  "isImportant": false,
  "folderName": "INBOX",
  "hasAttachments": true,
  "bodyText": "Welcome to Whale API Server. This is the full email content...",
  "bodyHtml": "<html><body>Welcome to Whale API Server...</body></html>",
  "bodyPreview": "Welcome to Whale API Server...",
  "createdDate": "2024-01-01T10:01:00Z",
  "modifiedDate": "2024-01-01T10:01:00Z"
}
```

### 3. 이메일 읽음 상태 변경

**PUT** `/email/{emailId}/read`

이메일의 읽음 상태를 변경합니다.

**Path Parameters:**
- `emailId`: 이메일 ID

**Query Parameters:**
- `userId` (required): 사용자 ID
- `isRead` (required): 읽음 상태 (true/false)

**Response:**
```json
{
  "success": true,
  "message": "Email read status updated successfully"
}
```

## 첨부파일 관리 API

### 1. 이메일 첨부파일 목록 조회

**GET** `/email/attachments/email/{emailId}`

특정 이메일의 첨부파일 목록을 조회합니다.

**Path Parameters:**
- `emailId`: 이메일 ID

**Query Parameters:**
- `userId` (required): 사용자 ID

**Response:**
```json
[
  {
    "identifier": "550e8400-e29b-41d4-a716-446655440003",
    "emailIdentifier": "550e8400-e29b-41d4-a716-446655440002",
    "attachmentId": "ATT001",
    "filename": "document.pdf",
    "mimeType": "application/pdf",
    "sizeBytes": 1024000,
    "isInline": false,
    "contentId": null,
    "hasLocalFile": true,
    "createdDate": "2024-01-01T10:01:00Z",
    "modifiedDate": "2024-01-01T10:01:00Z"
  }
]
```

### 2. 첨부파일 정보 조회

**GET** `/email/attachments/{attachmentId}`

특정 첨부파일의 정보를 조회합니다.

**Path Parameters:**
- `attachmentId`: 첨부파일 ID

**Query Parameters:**
- `userId` (required): 사용자 ID

**Response:**
```json
{
  "identifier": "550e8400-e29b-41d4-a716-446655440003",
  "emailIdentifier": "550e8400-e29b-41d4-a716-446655440002",
  "attachmentId": "ATT001",
  "filename": "document.pdf",
  "mimeType": "application/pdf",
  "sizeBytes": 1024000,
  "isInline": false,
  "contentId": null,
  "hasLocalFile": true,
  "createdDate": "2024-01-01T10:01:00Z",
  "modifiedDate": "2024-01-01T10:01:00Z"
}
```

### 3. 첨부파일 다운로드

**GET** `/email/attachments/{attachmentId}/download`

첨부파일을 다운로드합니다.

**Path Parameters:**
- `attachmentId`: 첨부파일 ID

**Query Parameters:**
- `userId` (required): 사용자 ID
- `emailId` (required): 이메일 ID

**Response:**
- **Content-Type**: 첨부파일의 MIME 타입
- **Content-Disposition**: `attachment; filename="document.pdf"`
- **Body**: 첨부파일 바이너리 데이터

### 4. 첨부파일 미리보기

**GET** `/email/attachments/{attachmentId}/preview`

첨부파일을 미리보기합니다. (이미지, PDF, 텍스트 파일 등)

**Path Parameters:**
- `attachmentId`: 첨부파일 ID

**Query Parameters:**
- `userId` (required): 사용자 ID
- `emailId` (required): 이메일 ID

**Response:**
- **Content-Type**: 첨부파일의 MIME 타입
- **Body**: 첨부파일 바이너리 데이터

## 동기화 API

### 1. 수동 이메일 동기화

**POST** `/email/sync`

특정 계정의 이메일을 수동으로 동기화합니다.

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "accountId": "550e8400-e29b-41d4-a716-446655440001",
  "folderName": "INBOX",
  "fullSync": false
}
```

**Response:**
```json
{
  "success": true,
  "message": "Email sync completed successfully",
  "syncedCount": 25,
  "newEmailCount": 5,
  "updatedEmailCount": 20
}
```

## Gmail OAuth API

### 1. Gmail 인증 URL 생성

**GET** `/email/gmail/auth-url`

Gmail OAuth2 인증을 위한 URL을 생성합니다.

**Query Parameters:**
- `userId` (required): 사용자 ID

**Response:**
```json
{
  "authUrl": "https://accounts.google.com/o/oauth2/auth?client_id=...&redirect_uri=...&scope=...&response_type=code&state=..."
}
```

### 2. Gmail OAuth 콜백 처리

**GET** `/email/gmail/callback`

Gmail OAuth2 콜백을 처리합니다.

**Query Parameters:**
- `code` (required): OAuth2 인증 코드
- `state` (required): 상태 값

**Response:**
```json
{
  "success": true,
  "message": "Gmail OAuth callback processed successfully",
  "accessToken": "ya29.a0AfH6SMC...",
  "refreshToken": "1//04...",
  "expiresIn": 3600
}
```

## 에러 코드

### HTTP 상태 코드

| 상태 코드 | 설명 |
|----------|------|
| 200 | 성공 |
| 201 | 생성됨 |
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 (중복 등록 등) |
| 500 | 서버 내부 오류 |

### 에러 응답 형식

```json
{
  "error": {
    "code": "EMAIL_ACCOUNT_NOT_FOUND",
    "message": "Email account not found",
    "details": "The specified email account does not exist or you don't have permission to access it."
  },
  "timestamp": "2024-01-01T10:00:00Z",
  "path": "/email/accounts/550e8400-e29b-41d4-a716-446655440001"
}
```

### 주요 에러 코드

| 에러 코드 | 설명 |
|----------|------|
| `EMAIL_ACCOUNT_NOT_FOUND` | 이메일 계정을 찾을 수 없음 |
| `EMAIL_ACCOUNT_ALREADY_EXISTS` | 이메일 계정이 이미 존재함 |
| `GMAIL_AUTH_FAILED` | Gmail 인증 실패 |
| `NAVER_AUTH_FAILED` | Naver 인증 실패 |
| `EMAIL_SYNC_FAILED` | 이메일 동기화 실패 |
| `ATTACHMENT_NOT_FOUND` | 첨부파일을 찾을 수 없음 |
| `ATTACHMENT_DOWNLOAD_FAILED` | 첨부파일 다운로드 실패 |
| `INVALID_EMAIL_PROVIDER` | 지원하지 않는 이메일 제공업체 |
| `TOKEN_EXPIRED` | 토큰 만료 |
| `INSUFFICIENT_PERMISSIONS` | 권한 부족 |

---

## 📝 참고사항

1. **Rate Limiting**: API 호출은 사용자당 분당 100회로 제한됩니다.
2. **Pagination**: 목록 조회 API는 기본적으로 20개씩 페이징됩니다.
3. **Caching**: 이메일 데이터는 15분간 캐시됩니다.
4. **Security**: 모든 민감한 데이터는 암호화되어 저장됩니다.
5. **Async Processing**: 대용량 동기화 작업은 비동기로 처리됩니다.

---

**Last Updated**: 2024-01-01  
**API Version**: v1.0.0
