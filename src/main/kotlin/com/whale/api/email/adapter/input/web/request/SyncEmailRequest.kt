package com.whale.api.email.adapter.input.web.request

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class SyncEmailRequest(
    @field:NotNull(message = "사용자 ID는 필수입니다")
    val userId: UUID,
    
    val accountId: UUID?,
    
    val folderName: String?,
)
