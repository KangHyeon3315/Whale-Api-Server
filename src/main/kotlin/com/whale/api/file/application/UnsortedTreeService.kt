package com.whale.api.file.application

import com.whale.api.file.application.port.`in`.GetUnsortedTreeUseCase
import com.whale.api.file.application.port.`in`.SortType
import com.whale.api.file.application.port.out.ListDirectoryOutput
import com.whale.api.file.application.port.out.ValidateFilePathOutput
import com.whale.api.file.domain.FileTreeItem
import com.whale.api.file.domain.exception.InvalidPathException
import com.whale.api.file.domain.property.FileProperty
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

@Service
class UnsortedTreeService(
    private val fileProperty: FileProperty,
    private val validateFilePathOutput: ValidateFilePathOutput,
    private val listDirectoryOutput: ListDirectoryOutput,
) : GetUnsortedTreeUseCase {
    private val logger = KotlinLogging.logger {}

    override fun getUnsortedTree(
        path: String,
        cursor: String?,
        limit: Int,
        sort: SortType,
    ): List<FileTreeItem> {
        // Python 코드와 동일하게 공백을 +로 변환
        val normalizedPath = path.replace(" ", "+")

        logger.debug("get_unsorted_tree path: $normalizedPath, cursor: $cursor, limit: $limit")

        val filePath = Paths.get(fileProperty.basePath, normalizedPath).toString()

        // 경로 검증
        validateFilePathOutput.validatePath(normalizedPath)

        val fullPath = Paths.get(filePath)
        if (!Files.exists(fullPath) || !Files.isDirectory(fullPath)) {
            throw InvalidPathException()
        }

        // 디렉토리 리스팅
        var fileDetails = listDirectoryOutput.listDirectory(filePath)

        // 정렬
        fileDetails =
            when (sort) {
                SortType.NUMBER -> sortByNumber(fileDetails)
                SortType.NAME -> sortByName(fileDetails)
            }

        // 커서 처리
        cursor?.let { cursorName ->
            val cursorIndex = fileDetails.indexOfFirst { it.name == cursorName }
            if (cursorIndex != -1) {
                fileDetails = fileDetails.drop(cursorIndex + 1)
            }
        }

        // 리밋 적용
        if (fileDetails.size > limit) {
            fileDetails = fileDetails.take(limit)
        }

        return fileDetails
    }

    private fun sortByName(files: List<FileTreeItem>): List<FileTreeItem> {
        return files.sortedWith(compareBy<FileTreeItem> { !it.isDir }.thenBy { it.name })
    }

    private fun sortByNumber(files: List<FileTreeItem>): List<FileTreeItem> {
        return files.sortedWith { file1, file2 ->
            // 디렉토리 우선 정렬
            val dirComparison = file2.isDir.compareTo(file1.isDir)
            if (dirComparison != 0) {
                return@sortedWith dirComparison
            }

            // 숫자 추출 및 비교
            val numbers1 = extractNumbers(file1.name)
            val numbers2 = extractNumbers(file2.name)

            // 숫자 튜플 비교
            val numberComparison = compareNumberTuples(numbers1, numbers2)
            if (numberComparison != 0) {
                return@sortedWith numberComparison
            }

            // 마지막으로 이름으로 비교
            file1.name.compareTo(file2.name)
        }
    }

    private fun extractNumbers(name: String): List<Int> {
        val regex = Regex("(\\d+)")
        return regex.findAll(name).map { it.value.toInt() }.toList()
    }

    private fun compareNumberTuples(
        numbers1: List<Int>,
        numbers2: List<Int>,
    ): Int {
        val minSize = minOf(numbers1.size, numbers2.size)

        for (i in 0 until minSize) {
            val comparison = numbers1[i].compareTo(numbers2[i])
            if (comparison != 0) {
                return comparison
            }
        }

        // 길이가 다른 경우 더 짧은 것이 먼저
        return numbers1.size.compareTo(numbers2.size)
    }
}
