package com.whale.api.file.application.port.out

import java.util.UUID

interface DeleteFileHashOutput {
    fun deleteByFileIdentifier(fileIdentifier: UUID)
}
