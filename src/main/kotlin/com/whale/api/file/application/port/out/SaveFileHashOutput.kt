package com.whale.api.file.application.port.out

import com.whale.api.file.domain.File
import com.whale.api.file.domain.FileHash
import com.whale.api.file.domain.Tag

interface SaveFileHashOutput {

    fun save(fileHash: FileHash): FileHash
}
