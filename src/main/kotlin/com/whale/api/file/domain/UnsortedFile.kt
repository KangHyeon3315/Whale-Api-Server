package com.whale.api.file.domain

import java.util.UUID

class UnsortedFile(
    val identifier: UUID,
    val path: String,
    val name: String,
    val fileHash: String?,
    val encoding: String?
) {
}
