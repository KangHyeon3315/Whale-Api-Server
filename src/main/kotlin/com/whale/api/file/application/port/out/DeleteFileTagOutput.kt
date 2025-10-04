package com.whale.api.file.application.port.out

import java.util.UUID

interface DeleteFileTagOutput {
    fun deleteByFileIdentifier(fileIdentifier: UUID)
}
