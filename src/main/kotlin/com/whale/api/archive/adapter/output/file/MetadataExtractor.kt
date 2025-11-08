package com.whale.api.archive.adapter.output.file

import com.whale.api.archive.application.port.out.MetadataExtractionOutput
import com.whale.api.archive.domain.ArchiveMetadata
import com.whale.api.archive.domain.property.ArchiveProperty
import mu.KotlinLogging
import org.springframework.stereotype.Repository
import org.springframework.web.multipart.MultipartFile
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadata

@Repository
class MetadataExtractor(
    private val archiveProperty: ArchiveProperty,
) : MetadataExtractionOutput {
    private val logger = KotlinLogging.logger {}

    override fun extractMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val fileName = file.originalFilename ?: return emptyList()
        val extension = fileName.substringAfterLast('.', "")

        return when {
            archiveProperty.isImageFile(".$extension") -> extractImageMetadata(file, archiveItemIdentifier)
            archiveProperty.isVideoFile(".$extension") -> extractVideoMetadata(file, archiveItemIdentifier)
            archiveProperty.isTextFile(".$extension") -> extractTextMetadata(file, archiveItemIdentifier)
            archiveProperty.isDocumentFile(".$extension") -> extractDocumentMetadata(file, archiveItemIdentifier)
            else -> extractBasicFileMetadata(file, archiveItemIdentifier)
        }
    }

    override fun extractImageMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            file.inputStream.use { inputStream ->
                val readers = ImageIO.getImageReaders(inputStream)
                if (readers.hasNext()) {
                    val reader = readers.next()
                    reader.input = ImageIO.createImageInputStream(inputStream)

                    val metadata = reader.getImageMetadata(0)
                    if (metadata != null) {
                        extractImageIOMetadata(metadata, archiveItemIdentifier, metadataList)
                    }

                    reader.dispose()
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract image metadata for item: $archiveItemIdentifier" }
        }

        return metadataList
    }

    override fun extractVideoMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            // 기본 파일 정보 추가
            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "file_size",
                    file.size.toString(),
                ),
            )

            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "content_type",
                    file.contentType ?: "unknown",
                ),
            )

            // TODO: FFmpeg를 사용한 비디오 메타데이터 추출 구현
            // 현재는 기본 정보만 추출
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract video metadata for item: $archiveItemIdentifier" }
        }

        return metadataList
    }

    override fun extractLivePhotoMetadata(
        imageFile: MultipartFile,
        videoFile: MultipartFile?,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        // 이미지 메타데이터 추출
        metadataList.addAll(extractImageMetadata(imageFile, archiveItemIdentifier))

        // 라이브 포토 특정 메타데이터 추가
        metadataList.add(
            ArchiveMetadata.createLivePhotoMetadata(
                archiveItemIdentifier,
                "is_live_photo",
                "true",
            ),
        )

        if (videoFile != null) {
            metadataList.add(
                ArchiveMetadata.createLivePhotoMetadata(
                    archiveItemIdentifier,
                    "video_file_size",
                    videoFile.size.toString(),
                ),
            )

            metadataList.add(
                ArchiveMetadata.createLivePhotoMetadata(
                    archiveItemIdentifier,
                    "video_content_type",
                    videoFile.contentType ?: "unknown",
                ),
            )
        }

        return metadataList
    }

    private fun extractImageIOMetadata(
        metadata: IIOMetadata,
        archiveItemIdentifier: UUID,
        metadataList: MutableList<ArchiveMetadata>,
    ) {
        try {
            val formatNames = metadata.metadataFormatNames
            for (formatName in formatNames) {
                val tree = metadata.getAsTree(formatName)
                extractNodeMetadata(tree, archiveItemIdentifier, metadataList, formatName)
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract ImageIO metadata" }
        }
    }

    private fun extractNodeMetadata(
        node: org.w3c.dom.Node,
        archiveItemIdentifier: UUID,
        metadataList: MutableList<ArchiveMetadata>,
        formatName: String,
    ) {
        try {
            val attributes = node.attributes
            if (attributes != null) {
                for (i in 0 until attributes.length) {
                    val attr = attributes.item(i)
                    val key = "${formatName}_${node.nodeName}_${attr.nodeName}"
                    val value = attr.nodeValue

                    metadataList.add(
                        ArchiveMetadata.createExifMetadata(
                            archiveItemIdentifier,
                            key,
                            value,
                        ),
                    )
                }
            }

            // 자식 노드들도 처리
            val childNodes = node.childNodes
            for (i in 0 until childNodes.length) {
                extractNodeMetadata(childNodes.item(i), archiveItemIdentifier, metadataList, formatName)
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract node metadata" }
        }
    }

    override fun extractTextMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            // 기본 파일 정보
            metadataList.addAll(extractBasicFileMetadata(file, archiveItemIdentifier))

            // 텍스트 파일 특정 메타데이터
            file.inputStream.use { inputStream ->
                val content = inputStream.bufferedReader().use { it.readText() }

                // 파일 인코딩 감지
                val encoding = detectEncoding(file)
                if (encoding != null) {
                    metadataList.add(
                        ArchiveMetadata.createFileEncodingMetadata(
                            archiveItemIdentifier,
                            "encoding",
                            encoding,
                        ),
                    )
                }

                // 텍스트 통계
                metadataList.add(
                    ArchiveMetadata.createTextContentMetadata(
                        archiveItemIdentifier,
                        "character_count",
                        content.length.toString(),
                    ),
                )

                metadataList.add(
                    ArchiveMetadata.createTextContentMetadata(
                        archiveItemIdentifier,
                        "line_count",
                        content.lines().size.toString(),
                    ),
                )

                metadataList.add(
                    ArchiveMetadata.createTextContentMetadata(
                        archiveItemIdentifier,
                        "word_count",
                        content.split("\\s+".toRegex()).size.toString(),
                    ),
                )

                // JSON 파일의 경우 구조 분석
                if (file.originalFilename?.endsWith(".json") == true) {
                    try {
                        // JSON 유효성 검사 (간단한 방법)
                        val isValidJson =
                            content.trim().let {
                                (it.startsWith("{") && it.endsWith("}")) ||
                                    (it.startsWith("[") && it.endsWith("]"))
                            }

                        metadataList.add(
                            ArchiveMetadata.createTextContentMetadata(
                                archiveItemIdentifier,
                                "is_valid_json",
                                isValidJson.toString(),
                            ),
                        )
                    } catch (e: Exception) {
                        logger.warn(e) { "Failed to analyze JSON structure" }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract text metadata for item: $archiveItemIdentifier" }
        }

        return metadataList
    }

    override fun extractDocumentMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            // 기본 파일 정보
            metadataList.addAll(extractBasicFileMetadata(file, archiveItemIdentifier))

            // 문서 파일 특정 메타데이터
            val fileName = file.originalFilename ?: ""
            val extension = fileName.substringAfterLast('.', "").lowercase()

            metadataList.add(
                ArchiveMetadata.createDocumentPropertiesMetadata(
                    archiveItemIdentifier,
                    "document_type",
                    extension,
                ),
            )

            // TODO: Apache POI나 다른 라이브러리를 사용하여 문서 메타데이터 추출
            // 현재는 기본 정보만 추출
            when (extension) {
                "pdf" -> {
                    metadataList.add(
                        ArchiveMetadata.createDocumentPropertiesMetadata(
                            archiveItemIdentifier,
                            "document_format",
                            "Portable Document Format",
                        ),
                    )
                }
                "doc", "docx" -> {
                    metadataList.add(
                        ArchiveMetadata.createDocumentPropertiesMetadata(
                            archiveItemIdentifier,
                            "document_format",
                            "Microsoft Word Document",
                        ),
                    )
                }
                "xls", "xlsx" -> {
                    metadataList.add(
                        ArchiveMetadata.createDocumentPropertiesMetadata(
                            archiveItemIdentifier,
                            "document_format",
                            "Microsoft Excel Spreadsheet",
                        ),
                    )
                }
                "ppt", "pptx" -> {
                    metadataList.add(
                        ArchiveMetadata.createDocumentPropertiesMetadata(
                            archiveItemIdentifier,
                            "document_format",
                            "Microsoft PowerPoint Presentation",
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract document metadata for item: $archiveItemIdentifier" }
        }

        return metadataList
    }

    private fun extractBasicFileMetadata(
        file: MultipartFile,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "file_size",
                    file.size.toString(),
                ),
            )

            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "content_type",
                    file.contentType ?: "unknown",
                ),
            )

            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "original_filename",
                    file.originalFilename ?: "unknown",
                ),
            )
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract basic file metadata" }
        }

        return metadataList
    }

    private fun detectEncoding(file: MultipartFile): String? {
        return try {
            file.inputStream.use { inputStream ->
                val bytes = inputStream.readNBytes(1024) // 첫 1KB만 읽어서 인코딩 감지

                // 간단한 인코딩 감지 (BOM 체크)
                when {
                    bytes.size >= 3 && bytes[0] == 0xEF.toByte() &&
                        bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() -> "UTF-8"

                    bytes.size >= 2 && bytes[0] == 0xFF.toByte() &&
                        bytes[1] == 0xFE.toByte() -> "UTF-16LE"

                    bytes.size >= 2 && bytes[0] == 0xFE.toByte() &&
                        bytes[1] == 0xFF.toByte() -> "UTF-16BE"

                    else -> {
                        // ASCII 체크
                        val isAscii = bytes.all { it >= 0 && it <= 127 }
                        if (isAscii) "ASCII" else "UTF-8" // 기본값으로 UTF-8 사용
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to detect file encoding" }
            null
        }
    }

    override fun extractMetadataFromFile(
        filePath: String,
        fileName: String,
        mimeType: String,
        fileSize: Long,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val extension = fileName.substringAfterLast('.', "")

        return when {
            archiveProperty.isImageFile(".$extension") -> extractImageMetadataFromFile(filePath, fileSize, archiveItemIdentifier)
            archiveProperty.isVideoFile(
                ".$extension",
            ) -> extractVideoMetadataFromFile(filePath, fileName, mimeType, fileSize, archiveItemIdentifier)
            archiveProperty.isTextFile(".$extension") -> extractTextMetadataFromFile(filePath, fileName, fileSize, archiveItemIdentifier)
            archiveProperty.isDocumentFile(
                ".$extension",
            ) -> extractDocumentMetadataFromFile(fileName, mimeType, fileSize, archiveItemIdentifier)
            else -> extractBasicFileMetadataFromFile(fileName, mimeType, fileSize, archiveItemIdentifier)
        }
    }

    private fun extractImageMetadataFromFile(
        filePath: String,
        fileSize: Long,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            FileInputStream(filePath).use { inputStream ->
                val readers = ImageIO.getImageReaders(inputStream)
                if (readers.hasNext()) {
                    val reader = readers.next()
                    reader.input = ImageIO.createImageInputStream(inputStream)

                    val metadata = reader.getImageMetadata(0)
                    if (metadata != null) {
                        extractImageIOMetadata(metadata, archiveItemIdentifier, metadataList)
                    }

                    reader.dispose()
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract image metadata from file: $filePath" }
        }

        return metadataList
    }

    private fun extractVideoMetadataFromFile(
        filePath: String,
        fileName: String,
        mimeType: String,
        fileSize: Long,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "file_size",
                    fileSize.toString(),
                ),
            )

            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "content_type",
                    mimeType,
                ),
            )

            // TODO: FFmpeg를 사용한 비디오 메타데이터 추출 구현
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract video metadata from file: $filePath" }
        }

        return metadataList
    }

    private fun extractTextMetadataFromFile(
        filePath: String,
        fileName: String,
        fileSize: Long,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            val path = Paths.get(filePath)
            val content = Files.readString(path, Charsets.UTF_8)

            // 파일 인코딩 감지
            val encoding = detectEncodingFromFile(filePath)
            if (encoding != null) {
                metadataList.add(
                    ArchiveMetadata.createFileEncodingMetadata(
                        archiveItemIdentifier,
                        "encoding",
                        encoding,
                    ),
                )
            }

            // 텍스트 통계
            metadataList.add(
                ArchiveMetadata.createTextContentMetadata(
                    archiveItemIdentifier,
                    "character_count",
                    content.length.toString(),
                ),
            )

            metadataList.add(
                ArchiveMetadata.createTextContentMetadata(
                    archiveItemIdentifier,
                    "line_count",
                    content.lines().size.toString(),
                ),
            )

            metadataList.add(
                ArchiveMetadata.createTextContentMetadata(
                    archiveItemIdentifier,
                    "word_count",
                    content.split("\\s+".toRegex()).size.toString(),
                ),
            )

            // JSON 파일의 경우 구조 분석
            if (fileName.endsWith(".json")) {
                try {
                    val isValidJson =
                        content.trim().let {
                            (it.startsWith("{") && it.endsWith("}")) ||
                                (it.startsWith("[") && it.endsWith("]"))
                        }

                    metadataList.add(
                        ArchiveMetadata.createTextContentMetadata(
                            archiveItemIdentifier,
                            "is_valid_json",
                            isValidJson.toString(),
                        ),
                    )
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to analyze JSON structure" }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract text metadata from file: $filePath" }
        }

        return metadataList
    }

    private fun extractDocumentMetadataFromFile(
        fileName: String,
        mimeType: String,
        fileSize: Long,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "file_size",
                    fileSize.toString(),
                ),
            )

            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "content_type",
                    mimeType,
                ),
            )

            val extension = fileName.substringAfterLast('.', "").lowercase()

            metadataList.add(
                ArchiveMetadata.createDocumentPropertiesMetadata(
                    archiveItemIdentifier,
                    "document_type",
                    extension,
                ),
            )

            when (extension) {
                "pdf" -> {
                    metadataList.add(
                        ArchiveMetadata.createDocumentPropertiesMetadata(
                            archiveItemIdentifier,
                            "document_format",
                            "Portable Document Format",
                        ),
                    )
                }
                "doc", "docx" -> {
                    metadataList.add(
                        ArchiveMetadata.createDocumentPropertiesMetadata(
                            archiveItemIdentifier,
                            "document_format",
                            "Microsoft Word Document",
                        ),
                    )
                }
                "xls", "xlsx" -> {
                    metadataList.add(
                        ArchiveMetadata.createDocumentPropertiesMetadata(
                            archiveItemIdentifier,
                            "document_format",
                            "Microsoft Excel Spreadsheet",
                        ),
                    )
                }
                "ppt", "pptx" -> {
                    metadataList.add(
                        ArchiveMetadata.createDocumentPropertiesMetadata(
                            archiveItemIdentifier,
                            "document_format",
                            "Microsoft PowerPoint Presentation",
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract document metadata" }
        }

        return metadataList
    }

    private fun extractBasicFileMetadataFromFile(
        fileName: String,
        mimeType: String,
        fileSize: Long,
        archiveItemIdentifier: UUID,
    ): List<ArchiveMetadata> {
        val metadataList = mutableListOf<ArchiveMetadata>()

        try {
            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "file_size",
                    fileSize.toString(),
                ),
            )

            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "content_type",
                    mimeType,
                ),
            )

            metadataList.add(
                ArchiveMetadata.createCustomMetadata(
                    archiveItemIdentifier,
                    "original_filename",
                    fileName,
                ),
            )
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract basic file metadata" }
        }

        return metadataList
    }

    private fun detectEncodingFromFile(filePath: String): String? {
        return try {
            FileInputStream(filePath).use { inputStream ->
                val bytes = inputStream.readNBytes(1024)

                when {
                    bytes.size >= 3 && bytes[0] == 0xEF.toByte() &&
                        bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() -> "UTF-8"

                    bytes.size >= 2 && bytes[0] == 0xFF.toByte() &&
                        bytes[1] == 0xFE.toByte() -> "UTF-16LE"

                    bytes.size >= 2 && bytes[0] == 0xFE.toByte() &&
                        bytes[1] == 0xFF.toByte() -> "UTF-16BE"

                    else -> {
                        val isAscii = bytes.all { it >= 0 && it <= 127 }
                        if (isAscii) "ASCII" else "UTF-8"
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to detect file encoding" }
            null
        }
    }
}
