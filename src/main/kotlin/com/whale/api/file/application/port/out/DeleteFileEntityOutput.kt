package com.whale.api.file.application.port.out

import java.util.UUID

interface DeleteFileEntityOutput {
    fun deleteByIdentifier(identifier: UUID)
}
