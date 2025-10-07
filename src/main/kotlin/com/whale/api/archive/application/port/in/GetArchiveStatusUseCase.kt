package com.whale.api.archive.application.port.`in`

import com.whale.api.archive.domain.Archive
import java.util.UUID

interface GetArchiveStatusUseCase {
    fun getArchive(archiveIdentifier: UUID): Archive
    fun getAllArchives(): List<Archive>
}
