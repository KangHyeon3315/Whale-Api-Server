package com.whale.api.archive.application.port.`in`

import java.util.UUID

interface StartArchiveUseCase {
    fun startArchive(archiveIdentifier: UUID)
}
