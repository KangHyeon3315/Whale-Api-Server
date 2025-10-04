package com.whale.api.file.application.port.out

import com.whale.api.file.domain.UnsortedFile

interface SaveUnsortedFileOutput {
    fun save(unsortedFile: UnsortedFile): UnsortedFile

    fun saveAll(unsortedFiles: List<UnsortedFile>): List<UnsortedFile>

    fun updateFileHash(
        path: String,
        fileHash: String?,
    )

    fun updateEncoding(
        path: String,
        encoding: String?,
    )
}
