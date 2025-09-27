package com.whale.api.service.file

import com.whale.api.controller.file.request.*
import com.whale.api.controller.file.response.*
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import java.util.UUID

interface FileService {

    // 파일 트리 조회
    fun getUnsortedTree(path: String, cursor: String?, limit: Int, sort: String): FileTreeResponse

    // 썸네일 생성/조회
    fun getThumbnail(path: String): ResponseEntity<Resource>

    // 이미지 조회
    fun getImage(path: String): ResponseEntity<Resource>

    // 비디오 스트리밍
    fun getVideo(path: String, range: String?): ResponseEntity<Resource>

    // 파일 삭제 (경로로)
    fun deleteFileByPath(request: DeleteFileByPathRequest)

    // 파일 타입 조회
    fun findAllTypes(): List<String>

    // 태그 조회
    fun findAllTags(): List<TagResponse>

    // 파일 저장 (비동기)
    fun saveFile(request: SaveFileRequest): SaveFileResponse

    // 파일 조회
    fun findFile(fileIdentifier: UUID): FileResponse

    // 파일 수정
    fun updateFile(fileIdentifier: UUID, request: UpdateFileRequest): FileResponse

    // 파일 삭제
    fun deleteFile(fileIdentifier: UUID)

    // 파일 그룹 저장
    fun saveFileGroup(request: SaveFileGroupRequest): FileGroupResponse

    // 파일 그룹 조회
    fun findFileGroup(fileGroupIdentifier: UUID): FileGroupResponse

    // 파일 그룹 수정
    fun updateFileGroup(fileGroupIdentifier: UUID, request: UpdateFileGroupRequest): FileGroupResponse

    // 파일 그룹 삭제
    fun deleteFileGroup(fileGroupIdentifier: UUID)

    // 파일 그룹 검색
    fun searchFileGroups(
        type: String?,
        tags: String?,
        keyword: String?,
        sort: String,
        order: String,
        cursor: String?,
        limit: Int
    ): List<FileGroupResponse>

    // 파일 그룹 내 파일 조회
    fun findFileGroupFiles(
        fileGroupIdentifier: UUID,
        type: String?,
        tags: String?,
        keyword: String?,
        sort: String,
        order: String,
        cursor: String?,
        limit: Int
    ): List<FileResponse>
}
