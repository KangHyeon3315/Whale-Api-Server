package com.whale.api.service.file.util

import com.whale.api.controller.file.response.FileTreeItem
import org.springframework.stereotype.Component

@Component
class FileSortUtil {
    
    fun sortFiles(files: List<FileTreeItem>, sortType: String): List<FileTreeItem> {
        return when (sortType) {
            "number" -> sortByNumber(files)
            "name" -> sortByName(files)
            "size" -> sortBySize(files)
            "date" -> sortByDate(files)
            else -> sortByName(files)
        }
    }
    
    private fun sortByNumber(files: List<FileTreeItem>): List<FileTreeItem> {
        return files.sortedWith(
            compareBy<FileTreeItem> { !it.isDir }
                .thenBy { file ->
                    val numbers = Regex("(\\d+)").findAll(file.name)
                        .map { it.value.toInt() }
                        .toList()
                    numbers.firstOrNull() ?: 0
                }
                .thenBy { it.name }
        )
    }
    
    private fun sortByName(files: List<FileTreeItem>): List<FileTreeItem> {
        return files.sortedWith(
            compareBy<FileTreeItem> { !it.isDir }
                .thenBy { it.name }
        )
    }
    
    private fun sortBySize(files: List<FileTreeItem>): List<FileTreeItem> {
        return files.sortedWith(
            compareBy<FileTreeItem> { !it.isDir }
                .thenBy { it.name }
        )
    }

    private fun sortByDate(files: List<FileTreeItem>): List<FileTreeItem> {
        return files.sortedWith(
            compareBy<FileTreeItem> { !it.isDir }
                .thenBy { it.name }
        )
    }
    
    fun filterFilesByCursor(files: List<FileTreeItem>, cursor: String?): List<FileTreeItem> {
        return cursor?.let { cursorValue ->
            val cursorIndex = files.indexOfFirst { it.name == cursorValue }
            if (cursorIndex >= 0) {
                files.drop(cursorIndex + 1)
            } else {
                files
            }
        } ?: files
    }
}
