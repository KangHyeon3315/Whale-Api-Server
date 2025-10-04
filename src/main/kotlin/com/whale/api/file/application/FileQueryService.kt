package com.whale.api.file.application

import com.whale.api.file.application.port.`in`.GetFileUseCase
import com.whale.api.file.application.port.out.ReadFileOutput
import com.whale.api.file.application.port.out.ValidateFilePathOutput
import com.whale.api.file.domain.FileResource
import com.whale.api.file.domain.exception.UnsupportedMediaFileTypeException
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class FileQueryService(
    private val validateFilePathOutput: ValidateFilePathOutput,
    private val readFileOutput: ReadFileOutput,
) : GetFileUseCase {
    private val logger = KotlinLogging.logger {}

    override fun getImage(path: String): FileResource {
        logger.debug("Getting image: $path")

        val normalizedPath = path.replace(" ", "+")

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

        val normalizedPath = path.replace(" ", "+")

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
}
