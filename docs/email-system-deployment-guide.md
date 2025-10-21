# 📧 Email Management System 도입 가이드

## 📋 목차
- [사전 준비사항](#사전-준비사항)
- [1단계: 데이터베이스 설정](#1단계-데이터베이스-설정)
- [2단계: 환경 설정](#2단계-환경-설정)
- [3단계: 외부 API 설정](#3단계-외부-api-설정)
- [4단계: 보안 설정](#4단계-보안-설정)
- [5단계: 스케줄러 활성화](#5단계-스케줄러-활성화)
- [6단계: 테스트 및 검증](#6단계-테스트-및-검증)
- [7단계: 모니터링 설정](#7단계-모니터링-설정)
- [8단계: 운영 배포](#8단계-운영-배포)

## 사전 준비사항

### ✅ 체크리스트
- [ ] PostgreSQL 데이터베이스 준비
- [ ] Gmail API 프로젝트 생성 및 OAuth2 설정
- [ ] Naver 개발자 계정 및 앱 비밀번호 설정
- [ ] 첨부파일 저장용 디스크 공간 확보 (최소 10GB)
- [ ] SSL 인증서 준비 (HTTPS 필수)
- [ ] 모니터링 도구 준비 (선택사항)

### 🛠️ 필요한 권한
- 데이터베이스 DDL 권한 (테이블 생성)
- 파일 시스템 쓰기 권한 (`/app/email/attachments/`)
- 외부 API 호출 권한 (Gmail API, Naver IMAP)

## 1단계: 데이터베이스 설정

### 1.1 Flyway 마이그레이션 실행

```bash
# 개발 환경에서 마이그레이션 확인
./gradlew flywayInfo

# 마이그레이션 실행
./gradlew flywayMigrate

# 마이그레이션 상태 확인
./gradlew flywayInfo
```

### 1.2 테이블 생성 확인

```sql
-- 생성된 테이블 확인
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name LIKE 'email_%';

-- 예상 결과:
-- email_accounts
-- email_messages  
-- email_attachments
-- flyway_schema_history
```

### 1.3 인덱스 최적화 (선택사항)

```sql
-- 성능 최적화를 위한 추가 인덱스
CREATE INDEX CONCURRENTLY idx_email_messages_received_date 
ON email_messages (received_date DESC);

CREATE INDEX CONCURRENTLY idx_email_messages_folder_read 
ON email_messages (folder_name, is_read);

CREATE INDEX CONCURRENTLY idx_email_accounts_sync_date 
ON email_accounts (last_sync_date) 
WHERE is_active = true;
```

## 2단계: 환경 설정

### 2.1 application.yml 설정

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod,schedule  # schedule 프로필 활성화 필수
  
  datasource:
    url: jdbc:postgresql://localhost:5432/whale_db
    username: ${DB_USERNAME:whale_user}
    password: ${DB_PASSWORD:your_password}
    
  jpa:
    hibernate:
      ddl-auto: validate  # 운영환경에서는 validate 사용
    show-sql: false
    
  flyway:
    enabled: true
    locations: classpath:db/migration
    
# 이메일 시스템 설정
email:
  encryption:
    secret-key: ${EMAIL_ENCRYPTION_KEY:your-32-character-secret-key}
  
  attachment:
    base-path: ${EMAIL_ATTACHMENT_PATH:/app/email/attachments}
    max-file-size: ${EMAIL_MAX_FILE_SIZE:50MB}
    
  gmail:
    client-id: ${GMAIL_CLIENT_ID:your-gmail-client-id}
    client-secret: ${GMAIL_CLIENT_SECRET:your-gmail-client-secret}
    redirect-uri: ${GMAIL_REDIRECT_URI:https://your-domain.com/email/gmail/callback}
    
  naver:
    imap-host: imap.naver.com
    imap-port: 993
    smtp-host: smtp.naver.com
    smtp-port: 587

# 스케줄링 설정
scheduling:
  pool-size: ${SCHEDULING_POOL_SIZE:10}
  thread-name-prefix: "email-scheduler-"
  
# 로깅 설정
logging:
  level:
    com.whale.api.email: INFO
    org.springframework.scheduling: INFO
  file:
    name: /app/logs/whale-api.log
```

### 2.2 환경 변수 설정

```bash
# .env 파일 또는 시스템 환경 변수
export DB_USERNAME=whale_user
export DB_PASSWORD=your_secure_password
export EMAIL_ENCRYPTION_KEY=your-32-character-secret-key-here
export EMAIL_ATTACHMENT_PATH=/app/email/attachments
export GMAIL_CLIENT_ID=your-gmail-client-id.googleusercontent.com
export GMAIL_CLIENT_SECRET=your-gmail-client-secret
export GMAIL_REDIRECT_URI=https://your-domain.com/email/gmail/callback
```

## 3단계: 외부 API 설정

### 3.1 Gmail API 설정

#### Google Cloud Console 설정
1. **Google Cloud Console** 접속 (https://console.cloud.google.com)
2. **새 프로젝트 생성** 또는 기존 프로젝트 선택
3. **Gmail API 활성화**:
   ```
   APIs & Services > Library > Gmail API > Enable
   ```

4. **OAuth2 인증 정보 생성**:
   ```
   APIs & Services > Credentials > Create Credentials > OAuth 2.0 Client IDs
   ```

5. **OAuth 동의 화면 설정**:
   ```
   OAuth consent screen > External > Create
   - App name: Whale Email Manager
   - User support email: your-email@domain.com
   - Scopes: https://www.googleapis.com/auth/gmail.readonly
   ```

6. **리디렉션 URI 설정**:
   ```
   Authorized redirect URIs:
   - https://your-domain.com/email/gmail/callback
   - http://localhost:8080/email/gmail/callback (개발용)
   ```

#### 필요한 OAuth2 스코프
```
https://www.googleapis.com/auth/gmail.readonly
https://www.googleapis.com/auth/gmail.modify
```

### 3.2 Naver Mail 설정

#### Naver 앱 비밀번호 생성
1. **Naver 로그인** (https://nid.naver.com)
2. **내정보 > 보안설정**
3. **2단계 인증 설정** (필수)
4. **앱 비밀번호 생성**:
   ```
   앱 비밀번호 > 새 앱 비밀번호 생성
   - 앱 이름: Whale Email Manager
   - 생성된 16자리 비밀번호 저장
   ```

#### IMAP 설정 확인
```
IMAP 서버: imap.naver.com
포트: 993 (SSL)
SMTP 서버: smtp.naver.com  
포트: 587 (TLS)
```

## 4단계: 보안 설정

### 4.1 암호화 키 생성

```bash
# 32바이트 암호화 키 생성
openssl rand -base64 32

# 또는 Java에서 생성
java -cp . -c "
import javax.crypto.KeyGenerator;
import java.util.Base64;
KeyGenerator keyGen = KeyGenerator.getInstance(\"AES\");
keyGen.init(256);
System.out.println(Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded()));
"
```

### 4.2 디렉토리 권한 설정

```bash
# 첨부파일 저장 디렉토리 생성
sudo mkdir -p /app/email/attachments
sudo chown -R whale-user:whale-group /app/email/attachments
sudo chmod 750 /app/email/attachments

# 로그 디렉토리 생성
sudo mkdir -p /app/logs
sudo chown -R whale-user:whale-group /app/logs
sudo chmod 755 /app/logs
```

### 4.3 방화벽 설정

```bash
# 필요한 포트 열기
sudo ufw allow 8080/tcp  # API 서버
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 993/tcp   # IMAP SSL (Naver)
sudo ufw allow 587/tcp   # SMTP TLS (Naver)
```

## 5단계: 스케줄러 활성화

### 5.1 스케줄러 프로필 확인

```yaml
# application.yml에서 확인
spring:
  profiles:
    active: prod,schedule  # schedule 프로필 필수
```

### 5.2 스케줄러 동작 확인

```bash
# 애플리케이션 시작 후 로그 확인
tail -f /app/logs/whale-api.log | grep -i scheduler

# 예상 로그:
# [email-scheduler-1] EmailSyncScheduler : Starting sync for all active accounts
# [email-scheduler-2] TokenRefreshScheduler : Checking for expired tokens
# [email-scheduler-3] AttachmentCleanupScheduler : Starting attachment cleanup
```

### 5.3 스케줄러 상태 모니터링

```kotlin
// 관리자용 스케줄러 상태 확인 API (선택사항)
@RestController
@RequestMapping("/admin/scheduler")
class SchedulerStatusController {
    
    @GetMapping("/status")
    fun getSchedulerStatus(): Map<String, Any> {
        return mapOf(
            "emailSync" to mapOf(
                "lastRun" to emailSyncScheduler.getLastRunTime(),
                "nextRun" to emailSyncScheduler.getNextRunTime(),
                "status" to "ACTIVE"
            ),
            "tokenRefresh" to mapOf(
                "lastRun" to tokenRefreshScheduler.getLastRunTime(),
                "nextRun" to tokenRefreshScheduler.getNextRunTime(),
                "status" to "ACTIVE"
            )
        )
    }
}
```

## 6단계: 테스트 및 검증

### 6.1 기본 기능 테스트

```bash
# 1. 헬스 체크
curl -X GET http://localhost:8080/actuator/health

# 2. Gmail 인증 URL 생성 테스트
curl -X GET "http://localhost:8080/email/gmail/auth-url?userId=test-user-id" \
  -H "Authorization: Bearer your-jwt-token"

# 3. 이메일 계정 목록 조회 테스트
curl -X GET "http://localhost:8080/email/accounts?userId=test-user-id" \
  -H "Authorization: Bearer your-jwt-token"
```

### 6.2 통합 테스트 시나리오

#### 시나리오 1: Gmail 계정 등록
```bash
# 1. 인증 URL 생성
GET /email/gmail/auth-url?userId={userId}

# 2. 사용자가 Google에서 인증 후 콜백 처리
GET /email/gmail/callback?code={auth_code}&state={state}

# 3. 계정 등록
POST /email/accounts/register
{
  "userId": "test-user-id",
  "emailAddress": "test@gmail.com",
  "provider": "GMAIL",
  "gmailAuthCode": "received-auth-code"
}

# 4. 초기 동기화 확인
GET /email/accounts/{accountId}/emails?userId={userId}
```

#### 시나리오 2: Naver 계정 등록
```bash
# 1. 계정 등록 (앱 비밀번호 사용)
POST /email/accounts/register
{
  "userId": "test-user-id",
  "emailAddress": "test@naver.com",
  "provider": "NAVER",
  "password": "16-digit-app-password"
}

# 2. 동기화 테스트
POST /email/sync
{
  "userId": "test-user-id",
  "accountId": "account-id",
  "folderName": "INBOX"
}
```

### 6.3 성능 테스트

```bash
# Apache Bench를 사용한 부하 테스트
ab -n 100 -c 10 -H "Authorization: Bearer your-jwt-token" \
  "http://localhost:8080/email/accounts?userId=test-user-id"

# 예상 결과:
# Requests per second: > 50 RPS
# Time per request: < 200ms (95th percentile)
```

## 7단계: 모니터링 설정

### 7.1 로그 모니터링

```yaml
# logback-spring.xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/app/logs/whale-api.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/app/logs/whale-api.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.whale.api.email" level="INFO"/>
    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 7.2 메트릭 수집 (선택사항)

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 7.3 알림 설정 (선택사항)

```kotlin
// Slack 알림 예제
@Component
class EmailAlertService {
    
    fun sendSyncFailureAlert(accountId: UUID, error: String) {
        // Slack Webhook 또는 이메일 알림
        val message = "Email sync failed for account: $accountId. Error: $error"
        slackWebhookService.sendAlert(message)
    }
}
```

## 8단계: 운영 배포

### 8.1 배포 전 체크리스트

- [ ] 모든 환경 변수 설정 완료
- [ ] 데이터베이스 마이그레이션 완료
- [ ] Gmail API 설정 완료
- [ ] Naver 앱 비밀번호 설정 완료
- [ ] 첨부파일 저장 디렉토리 권한 설정 완료
- [ ] SSL 인증서 설정 완료
- [ ] 방화벽 설정 완료
- [ ] 로그 로테이션 설정 완료
- [ ] 모니터링 설정 완료
- [ ] 백업 정책 수립 완료

### 8.2 배포 명령어

```bash
# 1. 애플리케이션 빌드
./gradlew clean build -x test

# 2. Docker 이미지 빌드 (선택사항)
docker build -t whale-api:latest .

# 3. 서비스 시작
java -jar -Dspring.profiles.active=prod,schedule \
  -Xms2g -Xmx4g \
  build/libs/whale-api-server.jar

# 또는 Docker로 실행
docker run -d \
  --name whale-api \
  -p 8080:8080 \
  -v /app/email/attachments:/app/email/attachments \
  -v /app/logs:/app/logs \
  --env-file .env \
  whale-api:latest
```

### 8.3 배포 후 검증

```bash
# 1. 서비스 상태 확인
curl http://localhost:8080/actuator/health

# 2. 스케줄러 동작 확인
tail -f /app/logs/whale-api.log | grep -i scheduler

# 3. 메모리 사용량 확인
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# 4. 데이터베이스 연결 확인
curl http://localhost:8080/actuator/health/db
```

## 🚨 주의사항

### 보안
- **암호화 키는 절대 코드에 하드코딩하지 마세요**
- **Gmail Client Secret은 안전한 곳에 보관하세요**
- **Naver 앱 비밀번호는 정기적으로 갱신하세요**

### 성능
- **첨부파일 저장 공간을 정기적으로 모니터링하세요**
- **데이터베이스 인덱스를 정기적으로 최적화하세요**
- **로그 파일 크기를 관리하세요**

### 운영
- **스케줄러 실행 시간을 트래픽이 적은 시간대로 조정하세요**
- **Gmail API 할당량을 모니터링하세요**
- **정기적인 백업을 수행하세요**

---

## 📞 지원

문제가 발생하면 다음을 확인하세요:
1. 로그 파일: `/app/logs/whale-api.log`
2. 데이터베이스 연결 상태
3. 외부 API 응답 상태
4. 디스크 공간 및 메모리 사용량

---

**Last Updated**: 2024-01-01  
**Version**: 1.0.0
