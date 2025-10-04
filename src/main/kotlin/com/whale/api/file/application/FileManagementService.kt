package com.whale.api.file.application

import com.whale.api.file.application.port.`in`.DeleteFileUseCase
import com.whale.api.file.application.port.`in`.GetAllTagsUseCase
import com.whale.api.file.application.port.`in`.GetFileTypesUseCase
import com.whale.api.file.application.port.out.DeleteFileOutput
import com.whale.api.file.application.port.out.FindAllTagsOutput
import com.whale.api.file.application.port.out.FindFileTypesOutput
import com.whale.api.file.application.port.out.ValidateFilePathOutput
import com.whale.api.file.domain.Tag
import com.whale.api.file.domain.exception.InvalidPathException
import com.whale.api.file.domain.property.FileProperty
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

@Service
class FileManagementService(
    private val fileProperty: FileProperty,
    private val validateFilePathOutput: ValidateFilePathOutput,
    private val deleteFileOutput: DeleteFileOutput,
    private val findFileTypesOutput: FindFileTypesOutput,
    private val findAllTagsOutput: FindAllTagsOutput,
) : DeleteFileUseCase,
    GetFileTypesUseCase,
    GetAllTagsUseCase {
    private val logger = KotlinLogging.logger {}

    override fun deleteUnsortedFileByPath(path: String) {
        // Python 코드와 동일하게 base_path와 path를 조합
        val fullPath = Paths.get(fileProperty.basePath, fileProperty.unsortedPath, path).toString()
        logger.info("delete by file_path: $fullPath")

        // 경로 검증
        validateFilePathOutput.validatePath(Paths.get(fileProperty.unsortedPath, path).toString())

        // 파일인지 확인 (Python 코드의 os.path.isfile 체크)
        val filePath = Paths.get(fullPath)
        if (!Files.isRegularFile(filePath)) {
            throw InvalidPathException()
        }

        // 파일 삭제
        deleteFileOutput.deleteFile(fullPath)
    }

    override fun getAllFileTypes(): List<String> {
        return findFileTypesOutput.findAllDistinctTypes()
    }

    override fun getAllTags(): List<Tag> {
        return findAllTagsOutput.findAllTags()
    }
}
