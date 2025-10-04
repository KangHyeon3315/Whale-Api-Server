package com.whale.api.file.application

import com.whale.api.file.application.port.`in`.GetFileUseCase
import com.whale.api.file.application.port.`in`.GetThumbnailUseCase
import com.whale.api.file.application.port.out.CreateThumbnailOutput
import com.whale.api.file.application.port.out.GetThumbnailOutput
import com.whale.api.file.application.port.out.ReadFileOutput
import com.whale.api.file.application.port.out.ValidateFilePathOutput
import com.whale.api.file.domain.FileResource
import com.whale.api.file.domain.exception.UnsupportedMediaFileTypeException
import com.whale.api.file.domain.property.FileProperty
import mu.KotlinLogging
import org.aspectj.util.FileUtil.normalizedPath
import org.springframework.stereotype.Service
import java.nio.file.Paths

@Service
class FileQueryService(
    private val validateFilePathOutput: ValidateFilePathOutput,
    private val readFileOutput: ReadFileOutput,
    private val createThumbnailOutput: CreateThumbnailOutput,
    private val getThumbnailOutput: GetThumbnailOutput,
    private val fileProperty: FileProperty,
) : GetFileUseCase,
    GetThumbnailUseCase {
    private val logger = KotlinLogging.logger {}

    override fun getUnsortedImage(path: String): FileResource {
        logger.debug("Getting image: $path")

        val normalizedPath = Paths.get(fileProperty.unsortedPath, path.replace(" ", "+")).toString()
        validateFilePathOutput.validatePath(normalizedPath)

        if (!validateFilePathOutput.isImageFile(normalizedPath)) {
            throw UnsupportedMediaFileTypeException("File is not a supported image type")
        }

        return readFileOutput.readFile(normalizedPath)
    }

    override fun getVideo(
        path: String,
        rangeHeader: String?,
    ): FileResource {
        logger.debug("Getting video: $path, rangeHeader: $rangeHeader")

        val normalizedPath = Paths.get(fileProperty.unsortedPath, path.replace(" ", "+")).toString()

        validateFilePathOutput.validatePath(normalizedPath)

        if (!validateFilePathOutput.isVideoFile(normalizedPath)) {
            throw UnsupportedMediaFileTypeException("File is not a supported video type")
        }

        return if (rangeHeader != null) {
            readFileOutput.readFileWithRange(normalizedPath, rangeHeader)
        } else {
            readFileOutput.readFile(normalizedPath)
        }
    }

    override fun getUnsortedThumbnail(path: String): FileResource {
        logger.debug("Getting thumbnail for path: $path")

        val normalizedPath = Paths.get(fileProperty.unsortedPath, path.replace(" ", "+")).toString()

        // 경로 검증
        validateFilePathOutput.validatePath(normalizedPath)

        // 지원되는 미디어 파일인지 확인
        if (!validateFilePathOutput.isImageFile(normalizedPath) && !validateFilePathOutput.isVideoFile(normalizedPath)) {
            throw UnsupportedMediaFileTypeException("File is not a supported media type")
        }

        // 썸네일 생성 (이미 존재하면 기존 것 사용)
        val thumbnailPath = createThumbnailOutput.createThumbnail(normalizedPath)

        // 썸네일 파일 반환
        return getThumbnailOutput.getThumbnail(thumbnailPath)
    }
}
