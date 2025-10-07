# Archive API Documentation

## Overview
Whale API Server의 iOS 갤러리 백업 관련 API 문서입니다.

### 주요 기능
- iOS 갤러리 파일 백업 (이미지, 비디오, 라이브 포토)
- 텍스트 파일 및 문서 파일 백업
- 메타데이터 자동 추출 및 저장 (EXIF, GPS, 텍스트 내용 등)
- 백업 진행 상태 실시간 추적
- 파일 중복 검사 (체크섬 기반)
- 파일 카테고리별 분류 및 조회
- 텍스트 파일 내용 조회 및 미리보기
- 태그 기반 파일 분류 및 검색

### 지원 파일 타입
- **이미지**: `.jpg`, `.jpeg`, `.png`, `.gif`, `.bmp`, `.webp`, `.tiff`, `.heic`, `.heif`
- **비디오**: `.mp4`, `.mov`, `.m4v`
- **텍스트**: `.txt`, `.md`, `.json`, `.xml`, `.csv`, `.log`, `.rtf`
- **문서**: `.doc`, `.docx`, `.pdf`, `.xls`, `.xlsx`, `.ppt`, `.pptx`

## Authentication
모든 API는 JWT 인증이 필요합니다. 요청 헤더에 `Authorization: Bearer <token>`을 포함해야 합니다.

## APIs

### 1. 백업 작업 생성

**POST** `/archives`

새로운 백업 작업을 생성합니다.

#### Request Body
```json
{
  "name": "iPhone 갤러리 백업 2024-01",
  "description": "2024년 1월 iPhone 갤러리 전체 백업",
  "totalItems": 150
}
```

#### Request Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 |
|-------------|------|----------|------|
| name | String | No | 백업 작업 이름 |
| description | String | Yes | 백업 작업 설명 |
| totalItems | Integer | Yes | 전체 파일 개수 (설정 시 자동 완료, 기본값: 0) |

#### Response
```json
{
  "identifier": "550e8400-e29b-41d4-a716-446655440000",
  "name": "iPhone 갤러리 백업 2024-01",
  "description": "2024년 1월 iPhone 갤러리 전체 백업",
  "status": "PENDING",
  "totalItems": 0,
  "processedItems": 0,
  "failedItems": 0,
  "progressPercentage": 0.0,
  "createdDate": "2024-01-15T10:30:00Z",
  "modifiedDate": "2024-01-15T10:30:00Z",
  "completedDate": null
}
```

#### Request Example
```bash
curl -X POST http://localhost:8080/archives \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 갤러리 백업 2024-01",
    "description": "2024년 1월 iPhone 갤러리 전체 백업"
  }'
```

### 2. 백업 작업 시작

**POST** `/archives/{archiveId}/start`

생성된 백업 작업을 시작합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| archiveId | UUID | 백업 작업 식별자 |

#### Response
```
200 OK
```

#### Request Example
```bash
curl -X POST http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/start \
  -H "Authorization: Bearer <token>"
```

### 4. 모든 백업 작업 조회

**GET** `/archives`

모든 백업 작업 목록을 조회합니다.

#### Response
```json
[
  {
    "identifier": "550e8400-e29b-41d4-a716-446655440000",
    "name": "iPhone 갤러리 백업 2024-01",
    "description": "2024년 1월 iPhone 갤러리 전체 백업",
    "totalItems": 1500,
    "processedItems": 750,
    "failedItems": 5,
    "progressPercentage": 50.0,
    "isCompleted": false,
    "createdDate": "2024-01-15T10:30:00Z",
    "modifiedDate": "2024-01-15T11:45:00Z",
    "completedDate": null
  }
]
```

#### Request Example
```bash
curl -X GET http://localhost:8080/archives \
  -H "Authorization: Bearer <token>"
```

### 5. 특정 백업 작업 조회

**GET** `/archives/{archiveId}`

특정 백업 작업의 상태를 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| archiveId | UUID | 백업 작업 식별자 |

#### Response
```json
{
  "identifier": "550e8400-e29b-41d4-a716-446655440000",
  "name": "iPhone 갤러리 백업 2024-01",
  "description": "2024년 1월 iPhone 갤러리 전체 백업",
  "totalItems": 1500,
  "processedItems": 1495,
  "failedItems": 5,
  "progressPercentage": 99.67,
  "isCompleted": true,
  "createdDate": "2024-01-15T10:30:00Z",
  "modifiedDate": "2024-01-15T14:20:00Z",
  "completedDate": "2024-01-15T14:20:00Z"
}
```

#### Request Example
```bash
curl -X GET http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <token>"
```

### 3. 파일 업로드

**POST** `/archives/{archiveId}/items`

백업 작업에 파일을 업로드합니다. 멀티파트 폼 데이터를 사용합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| archiveId | UUID | 백업 작업 식별자 |

#### Form Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| file | MultipartFile | Yes | 업로드할 파일 |
| originalPath | String | Yes | iOS 기기에서의 원본 파일 경로 |
| isLivePhoto | Boolean | No | 라이브 포토 여부 (기본값: false) |
| livePhotoVideo | MultipartFile | No | 라이브 포토의 비디오 파일 |
| originalCreatedDate | ISO DateTime | No | 원본 파일 생성 날짜 |
| originalModifiedDate | ISO DateTime | No | 원본 파일 수정 날짜 |
| metadata | JSON String | No | 추가 메타데이터 (JSON 형태) |


#### Response
```json
{
  "identifier": "123e4567-e89b-12d3-a456-426614174000",
  "archiveIdentifier": "550e8400-e29b-41d4-a716-446655440000",
  "originalPath": "/var/mobile/Media/DCIM/100APPLE/IMG_0001.HEIC",
  "fileName": "IMG_0001.HEIC",
  "fileSize": 2048576,
  "mimeType": "image/heic",
  "fileCategory": "image",
  "isLivePhoto": true,
  "hasLivePhotoVideo": true,
  "checksum": "a1b2c3d4e5f6...",
  "originalCreatedDate": "2024-01-10T15:30:00Z",
  "originalModifiedDate": "2024-01-10T15:30:00Z",
  "createdDate": "2024-01-15T11:00:00Z",
  "modifiedDate": "2024-01-15T11:00:00Z"
}
```

#### Request Example
```bash
# 일반 이미지 업로드
curl -X POST http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items \
  -H "Authorization: Bearer <token>" \
  -F "file=@photo.jpg" \
  -F "originalPath=/var/mobile/Media/DCIM/100APPLE/IMG_0001.JPG" \
  -F "originalCreatedDate=2024-01-10T15:30:00Z"

# 라이브 포토 업로드
curl -X POST http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items \
  -H "Authorization: Bearer <token>" \
  -F "file=@livephoto.heic" \
  -F "livePhotoVideo=@livephoto.mov" \
  -F "originalPath=/var/mobile/Media/DCIM/100APPLE/IMG_0002.HEIC" \
  -F "isLivePhoto=true"

# 텍스트 파일 업로드 (메타데이터와 태그 포함)
curl -X POST http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items \
  -H "Authorization: Bearer <token>" \
  -F "file=@document.txt" \
  -F "originalPath=/var/mobile/Documents/document.txt" \
  -F 'metadata={"author":"John","category":"work"}'
```

### 6. 백업된 파일 목록 조회 및 검색

**GET** `/archives/{archiveId}/items`

특정 백업 작업의 파일 목록을 조회합니다. 파일명 검색과 태그 필터링이 가능합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| archiveId | UUID | 백업 작업 식별자 |

#### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|-------|------|
| fileName | String | No | - | 파일명 부분 검색 (대소문자 무시) |
| cursor | String | No | - | 페이지네이션 커서 (ISO 8601 날짜 형식) |
| limit | Integer | No | 20 | 페이지 크기 (최대 100) |

#### Response
```json
{
  "items": [
    {
      "identifier": "123e4567-e89b-12d3-a456-426614174000",
      "archiveIdentifier": "550e8400-e29b-41d4-a716-446655440000",
      "originalPath": "/var/mobile/Media/DCIM/100APPLE/IMG_0001.HEIC",
      "fileName": "IMG_0001.HEIC",
      "fileSize": 2048576,
      "mimeType": "image/heic",
      "fileCategory": "image",
      "isLivePhoto": true,
      "hasLivePhotoVideo": true,
      "checksum": "a1b2c3d4e5f6...",
      "originalCreatedDate": "2024-01-10T15:30:00Z",
      "originalModifiedDate": "2024-01-10T15:30:00Z",
      "createdDate": "2024-01-15T11:00:00Z",
      "modifiedDate": "2024-01-15T11:00:00Z"
    }
  ],
  "hasNext": true,
  "nextCursor": "2024-01-15T11:00:00Z",
  "totalCount": 1500
}
```

#### Request Examples
```bash
# 모든 파일 조회
curl -X GET http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items \
  -H "Authorization: Bearer <token>"

# 파일명으로 검색 (IMG가 포함된 파일들)
curl -X GET "http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items?fileName=IMG" \
  -H "Authorization: Bearer <token>"

# 페이지네이션 (첫 페이지, 10개씩)
curl -X GET "http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items?limit=10" \
  -H "Authorization: Bearer <token>"

# 페이지네이션 (다음 페이지)
curl -X GET "http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items?cursor=2024-01-15T11:00:00Z&limit=10" \
  -H "Authorization: Bearer <token>"
```

### 7. 특정 파일 조회

**GET** `/archives/items/{itemId}`

특정 백업 파일의 상세 정보를 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| itemId | UUID | 백업 파일 식별자 |

#### Response
```json
{
  "identifier": "123e4567-e89b-12d3-a456-426614174000",
  "archiveIdentifier": "550e8400-e29b-41d4-a716-446655440000",
  "originalPath": "/var/mobile/Media/DCIM/100APPLE/IMG_0001.HEIC",
  "fileName": "IMG_0001.HEIC",
  "fileSize": 2048576,
  "mimeType": "image/heic",
  "fileCategory": "image",
  "isLivePhoto": true,
  "hasLivePhotoVideo": true,
  "checksum": "a1b2c3d4e5f6...",
  "originalCreatedDate": "2024-01-10T15:30:00Z",
  "originalModifiedDate": "2024-01-10T15:30:00Z",
  "createdDate": "2024-01-15T11:00:00Z",
  "modifiedDate": "2024-01-15T11:00:00Z"
}
```

#### Request Example
```bash
curl -X GET http://localhost:8080/archives/items/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer <token>"
```

### 8. 파일 메타데이터 조회

**GET** `/archives/items/{itemId}/metadata`

특정 백업 파일의 메타데이터를 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| itemId | UUID | 백업 파일 식별자 |

#### Response
```json
[
  {
    "identifier": "meta-001",
    "archiveItemIdentifier": "123e4567-e89b-12d3-a456-426614174000",
    "metadataType": "EXIF",
    "key": "Camera_Make",
    "value": "Apple",
    "createdDate": "2024-01-15T11:00:00Z"
  },
  {
    "identifier": "meta-002",
    "archiveItemIdentifier": "123e4567-e89b-12d3-a456-426614174000",
    "metadataType": "GPS",
    "key": "Latitude",
    "value": "37.7749",
    "createdDate": "2024-01-15T11:00:00Z"
  },
  {
    "identifier": "meta-003",
    "archiveItemIdentifier": "123e4567-e89b-12d3-a456-426614174000",
    "metadataType": "TEXT_CONTENT",
    "key": "character_count",
    "value": "1024",
    "createdDate": "2024-01-15T11:00:00Z"
  }
]
```

#### Request Example
```bash
curl -X GET http://localhost:8080/archives/items/123e4567-e89b-12d3-a456-426614174000/metadata \
  -H "Authorization: Bearer <token>"
```

### 9. 카테고리별 파일 조회

**GET** `/archives/{archiveId}/items/category/{category}`

특정 백업 작업에서 특정 카테고리의 파일들만 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| archiveId | UUID | 백업 작업 식별자 |
| category | String | 파일 카테고리 (image, video, text, document, live_photo, other) |

#### Response
```json
[
  {
    "identifier": "text-001",
    "archiveIdentifier": "550e8400-e29b-41d4-a716-446655440000",
    "originalPath": "/var/mobile/Documents/notes.txt",
    "fileName": "notes.txt",
    "fileSize": 1024,
    "mimeType": "text/plain",
    "fileCategory": "text",
    "isLivePhoto": false,
    "hasLivePhotoVideo": false,
    "checksum": "b2c3d4e5f6g7...",
    "originalCreatedDate": "2024-01-12T09:15:00Z",
    "originalModifiedDate": "2024-01-12T09:15:00Z",
    "createdDate": "2024-01-15T11:30:00Z",
    "modifiedDate": "2024-01-15T11:30:00Z"
  }
]
```

#### Request Example
```bash
# 텍스트 파일만 조회
curl -X GET http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items/category/text \
  -H "Authorization: Bearer <token>"

# 이미지 파일만 조회
curl -X GET http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/items/category/image \
  -H "Authorization: Bearer <token>"
```

### 10. 백업 카테고리 통계 조회

**GET** `/archives/{archiveId}/categories`

특정 백업 작업의 파일 카테고리별 통계를 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| archiveId | UUID | 백업 작업 식별자 |

#### Response
```json
{
  "image": 1200,
  "video": 150,
  "text": 45,
  "document": 30,
  "live_photo": 80,
  "other": 10
}
```

#### Request Example
```bash
curl -X GET http://localhost:8080/archives/550e8400-e29b-41d4-a716-446655440000/categories \
  -H "Authorization: Bearer <token>"
```

### 11. 텍스트 파일 내용 조회

**GET** `/archives/items/{itemId}/content`

텍스트 파일의 전체 내용을 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| itemId | UUID | 백업 파일 식별자 (텍스트 파일만 가능) |

#### Response
```json
{
  "content": "This is the full content of the text file..."
}
```

#### Request Example
```bash
curl -X GET http://localhost:8080/archives/items/text-001/content \
  -H "Authorization: Bearer <token>"
```

### 12. 텍스트 파일 내용 미리보기

**GET** `/archives/items/{itemId}/content/preview`

텍스트 파일의 내용을 제한된 길이로 미리보기합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| itemId | UUID | 백업 파일 식별자 (텍스트 파일만 가능) |

#### Query Parameters
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|-------|------|
| maxLength | Integer | 1000 | 최대 문자 수 |

#### Response
```json
{
  "content": "This is the preview of the text file content...",
  "isPreview": "true"
}
```

#### Request Example
```bash
# 기본 미리보기 (1000자)
curl -X GET http://localhost:8080/archives/items/text-001/content/preview \
  -H "Authorization: Bearer <token>"

# 500자 미리보기
curl -X GET http://localhost:8080/archives/items/text-001/content/preview?maxLength=500 \
  -H "Authorization: Bearer <token>"
```

### 13. 백업 파일 다운로드

**GET** `/archives/items/{itemId}/file`

백업된 파일을 다운로드합니다. Range 헤더를 지원하여 부분 다운로드가 가능합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| itemId | UUID | 백업 파일 식별자 |

#### Headers
| 헤더 | 필수 | 설명 |
|-----|------|------|
| Range | No | 부분 다운로드를 위한 범위 지정 (예: bytes=0-1023) |

#### Response
- **Content-Type**: 파일의 MIME 타입
- **Content-Length**: 파일 크기 또는 요청된 범위 크기
- **Accept-Ranges**: bytes
- **Cache-Control**: public, max-age=3600
- **Content-Disposition**: inline; filename="파일명"

#### Status Codes
- **200**: 전체 파일 반환
- **206**: 부분 파일 반환 (Range 요청 시)

#### Request Example
```bash
# 전체 파일 다운로드
curl -X GET http://localhost:8080/archives/items/123e4567-e89b-12d3-a456-426614174000/file \
  -o downloaded_file.jpg

# 부분 다운로드 (첫 1KB)
curl -X GET http://localhost:8080/archives/items/123e4567-e89b-12d3-a456-426614174000/file \
  -H "Range: bytes=0-1023" \
  -o partial_file.jpg
```

### 14. 백업 파일 썸네일 조회

**GET** `/archives/items/{itemId}/thumbnail`

백업된 이미지나 비디오 파일의 썸네일을 조회합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| itemId | UUID | 백업 파일 식별자 (이미지 또는 비디오만 가능) |

#### Response
- **Content-Type**: image/jpeg
- **Cache-Control**: public, max-age=86400 (24시간)

#### Request Example
```bash
curl -X GET http://localhost:8080/archives/items/123e4567-e89b-12d3-a456-426614174000/thumbnail \
  -H "Authorization: Bearer <token>" \
  -o thumbnail.jpg
```

### 15. 라이브 포토 비디오 조회

**GET** `/archives/items/{itemId}/live-photo-video`

라이브 포토의 비디오 부분을 조회합니다. Range 헤더를 지원하여 스트리밍이 가능합니다.

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| itemId | UUID | 라이브 포토 백업 파일 식별자 |

#### Headers
| 헤더 | 필수 | 설명 |
|-----|------|------|
| Range | No | 스트리밍을 위한 범위 지정 (예: bytes=0-1048575) |

#### Response
- **Content-Type**: video/quicktime
- **Content-Length**: 비디오 파일 크기 또는 요청된 범위 크기
- **Accept-Ranges**: bytes
- **Cache-Control**: public, max-age=3600

#### Status Codes
- **200**: 전체 비디오 반환
- **206**: 부분 비디오 반환 (Range 요청 시)

#### Request Example
```bash
# 라이브 포토 비디오 스트리밍
curl -X GET http://localhost:8080/archives/items/123e4567-e89b-12d3-a456-426614174000/live-photo-video \
  -H "Range: bytes=0-1048575" \
  -o live_photo_video.mov
```

## Status Codes

| 상태 코드 | 설명 |
|---------|------|
| 200 | 성공 |
| 400 | 잘못된 요청 (파일 크기 초과, 지원하지 않는 파일 형식 등) |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없음 |
| 413 | 파일 크기가 너무 큼 |
| 500 | 서버 내부 오류 |

## Error Response Format

```json
{
  "error": "BAD_REQUEST",
  "message": "File extension not allowed: .exe",
  "timestamp": "2024-01-15T11:00:00Z"
}
```

## 자동 완료

Archive 생성 시 `totalItems`를 설정하면 자동 완료 기능이 활성화됩니다:

- **totalItems > 0**: 처리된 아이템 수가 totalItems에 도달하면 자동으로 완료 (`isCompleted: true`, `completedDate` 설정)
- **totalItems = 0**: 자동 완료 비활성화

예시:
```json
{
  "name": "iPhone 갤러리 백업",
  "totalItems": 100
}
```
→ 100개 파일 업로드 완료 시 자동으로 완료됨

### 완료 상태 확인

Archive의 완료 여부는 다음 필드로 확인할 수 있습니다:
- `isCompleted`: 완료 여부 (boolean)
- `completedDate`: 완료 날짜 (null이면 미완료)

## 페이지네이션

Archive items 조회 API는 커서 기반 페이지네이션을 지원합니다.

### 커서 기반 페이지네이션

- **정렬 기준**: `createdDate` (생성 일시) 내림차순
- **커서**: 마지막 아이템의 `createdDate` 값을 다음 요청의 `cursor`로 사용
- **페이지 크기**: `limit` 파라미터로 조절 (기본값: 20, 최대: 100)

### 응답 구조

```json
{
  "items": [...],           // 아이템 목록
  "hasNext": true,          // 다음 페이지 존재 여부
  "nextCursor": "2024-01-15T11:00:00Z",  // 다음 페이지 커서
  "totalCount": 1500        // 전체 아이템 수
}
```

### 페이지네이션 사용법

1. **첫 페이지**: `cursor` 없이 요청
2. **다음 페이지**: 응답의 `nextCursor` 값을 `cursor`로 사용
3. **마지막 페이지**: `hasNext`가 `false`일 때

## Metadata Types

추출되는 메타데이터 타입은 다음과 같습니다:

| 타입 | 설명 | 적용 파일 |
|-----|------|----------|
| EXIF | 이미지 메타데이터 (카메라 설정, 촬영 정보 등) | 이미지 파일 |
| GPS | 위치 정보 (위도, 경도 등) | 이미지, 비디오 파일 |
| CAMERA | 카메라 정보 (제조사, 모델 등) | 이미지, 비디오 파일 |
| DEVICE | 기기 정보 (iOS 버전, 기기 모델 등) | 모든 파일 |
| LIVE_PHOTO | 라이브 포토 관련 정보 | 라이브 포토 파일 |
| TEXT_CONTENT | 텍스트 내용 분석 (문자 수, 줄 수, 단어 수 등) | 텍스트 파일 |
| DOCUMENT_PROPERTIES | 문서 속성 (문서 타입, 포맷 등) | 문서 파일 |
| FILE_ENCODING | 파일 인코딩 정보 (UTF-8, ASCII 등) | 텍스트 파일 |
| CUSTOM | 사용자 정의 메타데이터 | 모든 파일 |

## File Categories

파일은 다음 카테고리로 자동 분류됩니다:

| 카테고리 | 설명 | 파일 확장자 |
|---------|------|------------|
| image | 이미지 파일 | .jpg, .jpeg, .png, .gif, .bmp, .webp, .tiff, .heic, .heif |
| video | 비디오 파일 | .mp4, .mov, .m4v |
| text | 텍스트 파일 | .txt, .md, .json, .xml, .csv, .log, .rtf |
| document | 문서 파일 | .doc, .docx, .pdf, .xls, .xlsx, .ppt, .pptx |
| live_photo | 라이브 포토 | .heic, .heif (라이브 포토로 표시된 경우) |
| other | 기타 파일 | 위에 해당하지 않는 모든 파일 |

## Notes

- 최대 파일 크기: 100MB (설정 가능)
- 라이브 포토는 이미지 파일과 비디오 파일을 함께 업로드해야 합니다
- 텍스트 파일의 인코딩은 자동으로 감지됩니다 (UTF-8, UTF-16, ASCII)
- 메타데이터 추출은 비동기로 처리되며, 실패해도 파일 업로드는 성공합니다
- 체크섬은 SHA-256 알고리즘을 사용하여 계산됩니다


