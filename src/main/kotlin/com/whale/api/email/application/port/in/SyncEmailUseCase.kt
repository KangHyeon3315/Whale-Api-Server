package com.whale.api.email.application.port.`in`

import java.util.UUID

interface SyncEmailUseCase {
    fun syncAllAccounts(userId: String)

    fun syncAccount(
        userId: String,
        accountId: UUID,
    )

    fun syncAccountFolder(
        userId: String,
        accountId: UUID,
        folderName: String,
    )
}
