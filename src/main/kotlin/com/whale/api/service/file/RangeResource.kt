package com.whale.api.service.file

import org.springframework.core.io.Resource
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.nio.file.Path

class RangeResource(
    private val filePath: Path,
    private val start: Long,
    private val contentLength: Long
) : Resource {

    override fun exists(): Boolean = filePath.toFile().exists()

    override fun isReadable(): Boolean = filePath.toFile().canRead()

    override fun isOpen(): Boolean = false

    override fun isFile(): Boolean = true

    override fun getURL(): URL = filePath.toUri().toURL()

    override fun getURI(): URI = filePath.toUri()

    override fun getFile(): File = filePath.toFile()

    override fun contentLength(): Long = contentLength

    override fun lastModified(): Long = filePath.toFile().lastModified()

    override fun createRelative(relativePath: String): Resource {
        throw UnsupportedOperationException("Cannot create relative resource for RangeResource")
    }

    override fun getFilename(): String? = filePath.fileName.toString()

    override fun getDescription(): String = "Range resource [${filePath}] (start=$start, length=$contentLength)"

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {
        val fileInputStream = FileInputStream(filePath.toFile())
        fileInputStream.skip(start)
        return RangeInputStream(fileInputStream, contentLength)
    }

    private class RangeInputStream(
        private val inputStream: InputStream,
        private val maxBytes: Long
    ) : InputStream() {
        private var bytesRead: Long = 0

        override fun read(): Int {
            if (bytesRead >= maxBytes) {
                return -1
            }
            val result = inputStream.read()
            if (result != -1) {
                bytesRead++
            }
            return result
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (bytesRead >= maxBytes) {
                return -1
            }
            val maxRead = minOf(len.toLong(), maxBytes - bytesRead).toInt()
            val result = inputStream.read(b, off, maxRead)
            if (result > 0) {
                bytesRead += result
            }
            return result
        }

        override fun close() {
            inputStream.close()
        }
    }
}
