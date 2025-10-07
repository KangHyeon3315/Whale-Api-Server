package com.whale.api.archive.application.port.`in`

import com.whale.api.archive.domain.ArchiveFileResource
import java.util.UUID

interface GetArchiveFileUseCase {
    fun getArchiveFile(itemIdentifier: UUID, rangeHeader: String? = null): ArchiveFileResource
    fun getArchiveFileThumbnail(itemIdentifier: UUID): ArchiveFileResource
    fun getLivePhotoVideo(itemIdentifier: UUID, rangeHeader: String? = null): ArchiveFileResource
}
