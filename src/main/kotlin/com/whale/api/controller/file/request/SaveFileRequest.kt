package com.whale.api.controller.file.request

data class SaveFileRequest(
    val name: String,
    val path: String,
    val type: String,
    val tags: List<TagRequest> = emptyList()
)

data class TagRequest(
    val name: String,
    val type: String
)
