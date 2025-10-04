# File API Documentation

## Overview
Whale API Server의 파일 관리 관련 API 문서입니다.

### 주요 기능
- 파일 저장 및 관리 (이미지, 비디오)
- 자동 썸네일 생성 및 캐싱
- 태그 기반 파일 분류
- HTTP Range 헤더를 통한 비디오 스트리밍
- 디렉토리 구조 탐색
- 파일 타입별 필터링

## Authentication
모든 API는 JWT 인증이 필요합니다. 요청 헤더에 `Authorization: Bearer <token>`을 포함해야 합니다.

## APIs

### 1. 파일 저장 요청

**POST** `/files`

파일 저장 작업을 요청합니다.

#### Request Body
```json
{
  "path": "images/photo.jpg",
  "tags": ["vacation", "beach"]
}
```

#### Request Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 |
|-------------|------|----------|------|
| path | String | No | 저장할 파일의 경로 |
| tags | List\<String> | Yes | 파일에 연결할 태그 목록 |

#### Response
```json
{
  "message": "OK"
}
```

#### Request Example
```bash
curl -X POST http://localhost:8080/files \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "path": "images/vacation/beach.jpg",
    "tags": ["vacation", "beach", "summer"]
  }'
```

#### Response Status
- **200 OK**: 저장 요청 성공

#### 특징
- 파일 저장은 비동기로 처리됩니다
- 태그는 선택사항이며, 파일 분류에 사용됩니다

---

### 2. 파일 조회

**GET** `/files`

경로를 통해 파일을 조회하고 스트리밍으로 반환합니다.

#### Request Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 |
|-------------|------|----------|------|
| path | String | No | 조회할 파일의 경로 |

#### Response
파일 데이터를 스트리밍으로 반환합니다.

#### Request Example
```bash
# 이미지 파일 조회
curl -X GET "http://localhost:8080/files?path=images/photo.jpg" \
  -H "Authorization: Bearer <token>" \
  --output photo.jpg

# 비디오 파일 조회 (Range 헤더 지원)
curl -X GET "http://localhost:8080/files?path=videos/movie.mp4" \
  -H "Authorization: Bearer <token>" \
  -H "Range: bytes=0-1023" \
  --output movie_part.mp4
```

#### Response Status
- **200 OK**: 파일 조회 성공 (전체 파일)
- **206 Partial Content**: 부분 파일 조회 성공 (Range 요청)
- **404 Not Found**: 파일을 찾을 수 없음

#### 특징
- 이미지와 비디오 파일을 지원합니다
- 비디오 파일의 경우 HTTP Range 헤더를 지원하여 부분 스트리밍이 가능합니다
- 적절한 MIME 타입으로 응답합니다

---

### 3. 파일 삭제

**DELETE** `/files`

지정된 경로의 파일을 삭제합니다.

#### Request Body
```json
{
  "path": "images/old_photo.jpg"
}
```

#### Request Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 |
|-------------|------|----------|------|
| path | String | No | 삭제할 파일의 경로 |

#### Response
```json
"OK"
```

#### Request Example
```bash
curl -X DELETE http://localhost:8080/files \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "path": "images/old_photo.jpg"
  }'
```

#### Response Status
- **200 OK**: 파일 삭제 성공

#### 특징
- 파일 시스템에서 실제 파일을 삭제합니다
- 관련된 데이터베이스 레코드도 함께 삭제됩니다

---

### 4. 파일 타입 목록 조회

**GET** `/files/types`

데이터베이스에 저장된 모든 고유한 파일 타입 목록을 조회합니다.

#### Request Parameters
요청 파라미터 없음

#### Response
```json
{
  "types": ["jpg", "png", "mp4", "avi", "gif"]
}
```

#### Response Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 |
|-------------|------|----------|------|
| types | List\<String> | No | 고유한 파일 확장자 목록 |

#### Request Example
```bash
curl -X GET http://localhost:8080/files/types \
  -H "Authorization: Bearer <token>"
```

#### Response Status
- **200 OK**: 조회 성공

#### 특징
- 데이터베이스에 실제로 저장된 파일들의 확장자만 반환합니다
- 파일 필터링이나 분류에 사용할 수 있습니다

---

### 5. 태그 목록 조회

**GET** `/files/tags`

데이터베이스에 저장된 모든 태그 목록을 조회합니다.

#### Request Parameters
요청 파라미터 없음

#### Response
```json
{
  "tags": [
    {
      "identifier": "550e8400-e29b-41d4-a716-446655440000",
      "name": "vacation",
      "type": "user"
    },
    {
      "identifier": "660e8400-e29b-41d4-a716-446655440001",
      "name": "beach",
      "type": "user"
    }
  ]
}
```

#### Response Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 |
|-------------|------|----------|------|
| tags | List\<TagInfo> | No | 태그 정보 목록 |
| tags[].identifier | String | No | 태그 고유 식별자 |
| tags[].name | String | No | 태그 이름 |
| tags[].type | String | No | 태그 타입 |

#### Request Example
```bash
curl -X GET http://localhost:8080/files/tags \
  -H "Authorization: Bearer <token>"
```

#### Response Status
- **200 OK**: 조회 성공

#### 특징
- 시스템에 등록된 모든 태그를 반환합니다
- 태그 기반 파일 검색이나 분류에 사용할 수 있습니다

---

### 6. 썸네일 조회

**GET** `/files/thumbnail`

파일의 썸네일을 조회합니다. 썸네일이 없으면 자동으로 생성합니다.

#### Request Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 |
|-------------|------|----------|------|
| path | String | No | 썸네일을 조회할 파일의 경로 |

#### Response
썸네일 이미지 데이터를 JPEG 형식으로 스트리밍 반환합니다.

#### Request Example
```bash
# 이미지 파일 썸네일 조회
curl -X GET "http://localhost:8080/files/thumbnail?path=images/photo.jpg" \
  -H "Authorization: Bearer <token>" \
  --output photo_thumbnail.jpg

# 비디오 파일 썸네일 조회
curl -X GET "http://localhost:8080/files/thumbnail?path=videos/movie.mp4" \
  -H "Authorization: Bearer <token>" \
  --output movie_thumbnail.jpg

# 공백이 포함된 경로
curl -X GET "http://localhost:8080/files/thumbnail?path=my%20photos/vacation.jpg" \
  -H "Authorization: Bearer <token>" \
  --output vacation_thumbnail.jpg
```

#### Response Status
- **200 OK**: 썸네일 조회 성공
- **400 Bad Request**: 잘못된 경로 또는 지원하지 않는 파일 형식
- **404 Not Found**: 원본 파일을 찾을 수 없음

#### Response Headers
- `Content-Type: image/jpeg`
- `Content-Length: <파일크기>`
- `Cache-Control: public, max-age=3600` (1시간 캐시)

#### 특징
- 이미지와 비디오 파일을 지원합니다
- 썸네일 크기는 512x512 픽셀로 생성됩니다
- 이미지: 비율을 유지하며 리사이즈
- 비디오: 1초 지점에서 프레임 추출
- 썸네일이 이미 존재하면 기존 파일을 반환합니다
- 생성된 썸네일은 서버에 캐시되어 재사용됩니다
- HTTP 캐시 헤더를 통해 클라이언트 캐싱을 지원합니다

---

### 7. 정리되지 않은 파일 트리 조회

**GET** `/files/unsorted/tree`

정리되지 않은 파일들의 디렉토리 구조를 트리 형태로 조회합니다.

#### Request Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 | 기본값 |
|-------------|------|----------|------|--------|
| path | String | Yes | 조회할 디렉토리 경로 | "" (루트) |
| cursor | String | Yes | 페이징을 위한 커서 (파일/디렉토리 이름) | - |
| limit | Integer | Yes | 조회할 최대 개수 | 20 |
| sort | String | Yes | 정렬 방식 | "name" |

#### Response
```json
{
  "files": [
    {
      "name": "images",
      "isDir": true,
      "extension": ""
    },
    {
      "name": "photo001.jpg",
      "isDir": false,
      "extension": "jpg"
    },
    {
      "name": "video001.mp4",
      "isDir": false,
      "extension": "mp4"
    }
  ]
}
```

#### Response Parameters
| 프로퍼티 이름 | 타입 | Nullable | 설명 |
|-------------|------|----------|------|
| files | List\<FileTreeItem> | No | 파일/디렉토리 목록 |
| files[].name | String | No | 파일/디렉토리 이름 |
| files[].isDir | Boolean | No | 디렉토리 여부 |
| files[].extension | String | No | 파일 확장자 (디렉토리의 경우 빈 문자열) |

#### Request Example
```bash
# 루트 디렉토리 조회
curl -X GET "http://localhost:8080/files/unsorted/tree" \
  -H "Authorization: Bearer <token>"

# 특정 디렉토리 조회
curl -X GET "http://localhost:8080/files/unsorted/tree?path=images/vacation" \
  -H "Authorization: Bearer <token>"

# 숫자 정렬로 조회
curl -X GET "http://localhost:8080/files/unsorted/tree?sort=number&limit=50" \
  -H "Authorization: Bearer <token>"

# 페이징 사용
curl -X GET "http://localhost:8080/files/unsorted/tree?cursor=photo010.jpg&limit=10" \
  -H "Authorization: Bearer <token>"
```

#### Response Status
- **200 OK**: 조회 성공
- **400 Bad Request**: 잘못된 경로 또는 파라미터

#### 특징
- 디렉토리와 파일을 구분하여 표시합니다
- 디렉토리가 파일보다 먼저 정렬됩니다
- `sort=name`: 이름순 정렬 (기본값)
- `sort=number`: 숫자 추출 후 숫자순 정렬
- 커서 기반 페이징을 지원합니다
- 경로에 공백이 있는 경우 `+`로 변환하여 처리합니다

---

## 데이터 타입 및 Enum

### SortType
- `name`: 이름순 정렬
- `number`: 숫자순 정렬 (파일명에서 숫자를 추출하여 정렬)

### 지원되는 파일 형식

#### 이미지 파일
- `.jpg`, `.jpeg` - JPEG 이미지
- `.png` - PNG 이미지
- `.gif` - GIF 이미지
- `.bmp` - BMP 이미지
- `.webp` - WebP 이미지
- `.tiff` - TIFF 이미지
- `.svg` - SVG 벡터 이미지

#### 비디오 파일
- `.mp4` - MP4 비디오
- `.avi` - AVI 비디오
- `.mkv` - Matroska 비디오
- `.mov` - QuickTime 비디오
- `.wmv` - Windows Media 비디오
- `.flv` - Flash 비디오
- `.webm` - WebM 비디오
- `.m4v` - iTunes 비디오

#### 썸네일 생성 규칙
- **이미지**: 원본 비율을 유지하며 512x512 픽셀 내로 리사이즈
- **비디오**: 1초 지점에서 프레임을 추출하여 512픽셀 너비로 생성
- **형식**: 모든 썸네일은 JPEG 형식으로 저장
- **저장 위치**: `{basePath}/thumbnail/{originalPath}.thumbnail.jpg`

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "잘못된 요청입니다."
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "인증이 필요합니다."
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "요청한 파일을 찾을 수 없습니다."
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "서버 내부 오류가 발생했습니다."
}
```

---

## 사용 예시

### 1. 파일 업로드 및 태그 지정
```bash
# 1. 파일 저장 요청
curl -X POST http://localhost:8080/files \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "path": "photos/2024/vacation/beach.jpg",
    "tags": ["vacation", "beach", "2024", "summer"]
  }'

# 2. 태그 목록 확인
curl -X GET http://localhost:8080/files/tags \
  -H "Authorization: Bearer <token>"
```

### 2. 파일 브라우징
```bash
# 1. 정리되지 않은 파일 트리 조회
curl -X GET "http://localhost:8080/files/unsorted/tree" \
  -H "Authorization: Bearer <token>"

# 2. 특정 디렉토리 내용 조회
curl -X GET "http://localhost:8080/files/unsorted/tree?path=photos/2024" \
  -H "Authorization: Bearer <token>"

# 3. 파일 다운로드
curl -X GET "http://localhost:8080/files?path=photos/2024/vacation/beach.jpg" \
  -H "Authorization: Bearer <token>" \
  --output beach.jpg
```

### 3. 파일 관리
```bash
# 1. 파일 타입 목록 조회
curl -X GET http://localhost:8080/files/types \
  -H "Authorization: Bearer <token>"

# 2. 불필요한 파일 삭제
curl -X DELETE http://localhost:8080/files \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "path": "temp/old_file.jpg"
  }'
```

### 4. 비디오 스트리밍
```bash
# 1. 비디오 파일 부분 다운로드 (첫 1MB)
curl -X GET "http://localhost:8080/files?path=videos/movie.mp4" \
  -H "Authorization: Bearer <token>" \
  -H "Range: bytes=0-1048575" \
  --output movie_part1.mp4

# 2. 비디오 파일 전체 스트리밍
curl -X GET "http://localhost:8080/files?path=videos/movie.mp4" \
  -H "Authorization: Bearer <token>" \
  --output movie_full.mp4
```

### 5. 썸네일 활용
```bash
# 1. 이미지 썸네일 생성 및 조회
curl -X GET "http://localhost:8080/files/thumbnail?path=photos/2024/vacation/beach.jpg" \
  -H "Authorization: Bearer <token>" \
  --output beach_thumbnail.jpg

# 2. 비디오 썸네일 생성 및 조회
curl -X GET "http://localhost:8080/files/thumbnail?path=videos/family_trip.mp4" \
  -H "Authorization: Bearer <token>" \
  --output family_trip_thumbnail.jpg

# 3. 갤러리 뷰를 위한 여러 썸네일 조회
for file in photo001.jpg photo002.jpg photo003.jpg; do
  curl -X GET "http://localhost:8080/files/thumbnail?path=gallery/$file" \
    -H "Authorization: Bearer <token>" \
    --output "thumbnails/${file%.*}_thumb.jpg"
done

# 4. 웹 브라우저에서 썸네일 표시 (HTML 예시)
# <img src="http://localhost:8080/files/thumbnail?path=photos/image.jpg"
#      alt="Thumbnail"
#      style="width: 150px; height: 150px; object-fit: cover;">
```
