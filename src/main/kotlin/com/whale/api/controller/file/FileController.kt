package com.whale.api.controller.file

import com.whale.api.controller.file.request.*
import com.whale.api.controller.file.response.*
import com.whale.api.global.annotation.RequireAuth
import com.whale.api.global.annotation.WebController
import com.whale.api.service.file.FileService
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@WebController
@RequestMapping("/file")
class FileController(
    private val fileService: FileService,
) {

    @GetMapping("/unsorted/tree")
    @RequireAuth
    fun getUnsortedTree(
        @RequestParam(defaultValue = "") path: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "name") sort: String
    ): ResponseEntity<FileTreeResponse> {
        val decodedPath = path.replace(" ", "+")
        return ResponseEntity.ok(fileService.getUnsortedTree(decodedPath, cursor, limit, sort))
    }

    @GetMapping("/thumbnail")
    @RequireAuth
    fun getThumbnail(@RequestParam path: String): ResponseEntity<Resource> {
        val decodedPath = path.replace(" ", "+")
        return fileService.getThumbnail(decodedPath)
    }

    @GetMapping("/image")
    @RequireAuth
    fun getImage(@RequestParam path: String): ResponseEntity<Resource> {
        val decodedPath = path.replace(" ", "+")
        return fileService.getImage(decodedPath)
    }

    @GetMapping("/video")
    fun getVideo(
        @RequestParam path: String,
        @RequestHeader(value = "Range", required = false) range: String?
    ): ResponseEntity<Resource> {
        val decodedPath = path.replace(" ", "+")
        return fileService.getVideo(decodedPath, range)
    }

    @DeleteMapping
    @RequireAuth
    fun deleteFileByPath(@RequestBody request: DeleteFileByPathRequest): ResponseEntity<String> {
        fileService.deleteFileByPath(request)
        return ResponseEntity.ok("OK")
    }

    @GetMapping("/types")
    @RequireAuth
    fun findAllTypes(): ResponseEntity<Map<String, List<String>>> {
        return ResponseEntity.ok(mapOf("types" to fileService.findAllTypes()))
    }

    @GetMapping("/tags")
    @RequireAuth
    fun findAllTags(): ResponseEntity<Map<String, List<TagResponse>>> {
        return ResponseEntity.ok(mapOf("tags" to fileService.findAllTags()))
    }

    @PostMapping("/save")
    @RequireAuth
    fun saveFile(@RequestBody request: SaveFileRequest): ResponseEntity<SaveFileResponse> {
        return ResponseEntity.accepted().body(fileService.saveFile(request))
    }

    @GetMapping("/{fileIdentifier}")
    @RequireAuth
    fun findFile(@PathVariable fileIdentifier: UUID): ResponseEntity<FileResponse> {
        return ResponseEntity.ok(fileService.findFile(fileIdentifier))
    }

    @PutMapping("/{fileIdentifier}")
    @RequireAuth
    fun updateFile(
        @PathVariable fileIdentifier: UUID,
        @RequestBody request: UpdateFileRequest
    ): ResponseEntity<FileResponse> {
        return ResponseEntity.ok(fileService.updateFile(fileIdentifier, request))
    }

    @DeleteMapping("/{fileIdentifier}")
    @RequireAuth
    fun deleteFile(@PathVariable fileIdentifier: UUID): ResponseEntity<Void> {
        fileService.deleteFile(fileIdentifier)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/group")
    @RequireAuth
    fun saveFileGroup(@RequestBody request: SaveFileGroupRequest): ResponseEntity<FileGroupResponse> {
        return ResponseEntity.status(201).body(fileService.saveFileGroup(request))
    }

    @GetMapping("/group/{fileGroupIdentifier}")
    @RequireAuth
    fun findFileGroup(@PathVariable fileGroupIdentifier: UUID): ResponseEntity<FileGroupResponse> {
        return ResponseEntity.ok(fileService.findFileGroup(fileGroupIdentifier))
    }

    @PutMapping("/group/{fileGroupIdentifier}")
    @RequireAuth
    fun updateFileGroup(
        @PathVariable fileGroupIdentifier: UUID,
        @RequestBody request: UpdateFileGroupRequest
    ): ResponseEntity<FileGroupResponse> {
        return ResponseEntity.ok(fileService.updateFileGroup(fileGroupIdentifier, request))
    }

    @DeleteMapping("/group/{fileGroupIdentifier}")
    @RequireAuth
    fun deleteFileGroup(@PathVariable fileGroupIdentifier: UUID): ResponseEntity<Void> {
        fileService.deleteFileGroup(fileGroupIdentifier)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/group")
    @RequireAuth
    fun searchFileGroups(
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) tags: String?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "created_date") sort: String,
        @RequestParam(defaultValue = "desc") order: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<Map<String, List<FileGroupResponse>>> {
        val fileGroups = fileService.searchFileGroups(type, tags, keyword, sort, order, cursor, limit)
        return ResponseEntity.ok(mapOf("file_groups" to fileGroups))
    }

    @GetMapping("/group/{fileGroupIdentifier}/files")
    @RequireAuth
    fun findFileGroupFiles(
        @PathVariable fileGroupIdentifier: UUID,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) tags: String?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "created_date") sort: String,
        @RequestParam(defaultValue = "desc") order: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<Map<String, List<FileResponse>>> {
        val files = fileService.findFileGroupFiles(fileGroupIdentifier, type, tags, keyword, sort, order, cursor, limit)
        return ResponseEntity.ok(mapOf("files" to files))
    }
}
