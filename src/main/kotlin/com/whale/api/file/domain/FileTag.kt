package com.whale.api.file.domain

import java.util.UUID

class FileTag(
    val identifier: UUID,
    val fileIdentifier: UUID,
    val tagIdentifier: UUID
) {
}
