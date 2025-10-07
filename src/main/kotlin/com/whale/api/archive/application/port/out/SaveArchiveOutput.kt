package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.Archive

interface SaveArchiveOutput {
    fun save(archive: Archive): Archive
}
