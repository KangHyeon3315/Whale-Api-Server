package com.whale.api.archive.application.port.`in`

import com.whale.api.archive.application.port.`in`.command.CreateArchiveCommand
import com.whale.api.archive.domain.Archive

interface CreateArchiveUseCase {
    fun createArchive(command: CreateArchiveCommand): Archive
}
