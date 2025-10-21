package com.whale.api.email.adapter.input.web.request

import com.whale.api.email.application.port.`in`.command.RegisterEmailAccountCommand
import com.whale.api.email.domain.EmailProvider
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class RegisterEmailAccountRequest(
    @field:NotNull(message = "사용자 ID는 필수입니다")
    val userId: UUID,
    
    @field:NotBlank(message = "이메일 주소는 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val emailAddress: String,
    
    @field:NotNull(message = "이메일 제공업체는 필수입니다")
    val provider: EmailProvider,
    
    val displayName: String?,
    
    // Gmail용 OAuth2 인증 코드
    val authorizationCode: String?,
    
    // Naver용 비밀번호
    val password: String?,
) {
    fun toCommand(): RegisterEmailAccountCommand {
        return RegisterEmailAccountCommand(
            userId = this.userId,
            emailAddress = this.emailAddress,
            provider = this.provider,
            displayName = this.displayName,
            authorizationCode = this.authorizationCode,
            password = this.password,
        )
    }
    
    fun validate() {
        when (provider) {
            EmailProvider.GMAIL -> {
                require(!authorizationCode.isNullOrBlank()) {
                    "Gmail 계정 등록에는 인증 코드가 필요합니다"
                }
            }
            EmailProvider.NAVER -> {
                require(!password.isNullOrBlank()) {
                    "Naver 계정 등록에는 비밀번호가 필요합니다"
                }
            }
        }
    }
}
