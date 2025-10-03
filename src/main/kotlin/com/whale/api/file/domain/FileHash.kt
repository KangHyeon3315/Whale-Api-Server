package com.whale.api.file.domain

import java.util.UUID

class FileHash(
    val identifier: UUID,
    val fileIdentifier: UUID,
    val hash: String,
)
