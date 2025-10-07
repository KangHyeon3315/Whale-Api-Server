package com.whale.api.archive.application.port.`in`

import com.whale.api.archive.application.port.`in`.command.UploadArchiveItemCommand
import com.whale.api.archive.domain.ArchiveItem

interface UploadArchiveItemUseCase {
    fun uploadItem(command: UploadArchiveItemCommand): ArchiveItem
}
