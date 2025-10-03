package com.whale.api.file.application.port.`in`.command

import java.util.UUID


data class SaveFileCommand(
    val fileGroupIdentifier: UUID?,
    val name: String,
    val path: String,
    val type: String,
    val sortOrder: Int?,
    val tags: List<Tag>,
) {

    class Tag(
        val name: String,
        val type: String
    )

}
