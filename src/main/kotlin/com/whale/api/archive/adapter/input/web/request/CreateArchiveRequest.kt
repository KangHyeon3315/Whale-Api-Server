package com.whale.api.archive.adapter.input.web.request

import com.whale.api.archive.application.port.`in`.command.CreateArchiveCommand

data class CreateArchiveRequest(
    val name: String,
    val description: String?,
    val totalItems: Int = 0,
) {
    fun toCommand(): CreateArchiveCommand {
        return CreateArchiveCommand(
            name = this.name,
            description = this.description,
            totalItems = this.totalItems,
        )
    }
}
