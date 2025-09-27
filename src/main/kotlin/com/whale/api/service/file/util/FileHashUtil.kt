package com.whale.api.service.file.util

import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

@Component
class FileHashUtil {
    
    private val logger = KotlinLogging.logger {}
    
    fun calculateFileHash(filePath: Path): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            Files.newInputStream(filePath).use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            logger.error("Failed to calculate hash for file: $filePath", e)
            throw RuntimeException("Failed to calculate file hash", e)
        }
    }
    
    fun calculateFileHashSafely(filePath: Path): String? {
        return try {
            calculateFileHash(filePath)
        } catch (e: Exception) {
            logger.warn("Failed to calculate hash for file: $filePath", e)
            null
        }
    }
}
