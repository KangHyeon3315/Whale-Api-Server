package com.whale.api.file.adapter.input.web.request

import com.whale.api.file.application.port.`in`.command.SaveFileCommand
import java.util.UUID

data class SaveFileRequest(
    val fileGroupIdentifier: UUID?,
    val name: String,
    val path: String,
    val type: String,
    val sortOrder: Int?,
    val tags: List<TagRequest>,
) {
    data class TagRequest(
        val name: String,
        val type: String,
    )

    fun toCommand(): SaveFileCommand {
        return SaveFileCommand(
            fileGroupIdentifier = this.fileGroupIdentifier,
            name = this.name,
            path = this.path,
            type = this.type,
            sortOrder = this.sortOrder,
            tags = this.tags.map { 
                SaveFileCommand.Tag(
                    name = it.name,
                    type = it.type,
                )
            },
        )
    }
}
