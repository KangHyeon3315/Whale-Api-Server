package com.whale.api.file.adapter.input.web

import com.whale.api.file.adapter.input.web.request.SaveFileRequest
import com.whale.api.file.application.port.`in`.SaveFileUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/files")
class FileWebController(
    private val saveFileUseCase: SaveFileUseCase,
) {
    @PostMapping("/save-request")
    fun requestSave(@RequestBody request: SaveFileRequest): ResponseEntity<Void> {
        saveFileUseCase.requestSave(request.toCommand())
        return ResponseEntity.ok().build()
    }
}
