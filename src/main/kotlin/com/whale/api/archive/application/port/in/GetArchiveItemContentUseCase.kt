package com.whale.api.archive.application.port.`in`

import java.util.UUID

interface GetArchiveItemContentUseCase {
    fun getTextContent(itemIdentifier: UUID): String

    fun getTextContentPreview(
        itemIdentifier: UUID,
        maxLength: Int = 1000,
    ): String
}
