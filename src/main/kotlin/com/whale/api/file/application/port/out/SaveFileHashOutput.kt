package com.whale.api.file.application.port.out

import com.whale.api.file.domain.FileHash

interface SaveFileHashOutput {
    fun save(fileHash: FileHash): FileHash
}
