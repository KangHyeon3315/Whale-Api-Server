package com.whale.api.archive.application

import com.whale.api.archive.application.port.`in`.GetArchiveTagsUseCase
import com.whale.api.archive.application.port.`in`.ManageArchiveTagsUseCase
import com.whale.api.archive.application.port.out.FindArchiveItemOutput
import com.whale.api.archive.application.port.out.FindArchiveItemTagOutput
import com.whale.api.archive.application.port.out.FindArchiveTagOutput
import com.whale.api.archive.application.port.out.SaveArchiveItemTagOutput
import com.whale.api.archive.application.port.out.SaveArchiveTagOutput
import com.whale.api.archive.domain.ArchiveItemTag
import com.whale.api.archive.domain.ArchiveTag
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class ArchiveTagService(
    private val findArchiveTagOutput: FindArchiveTagOutput,
    private val saveArchiveTagOutput: SaveArchiveTagOutput,
    private val findArchiveItemTagOutput: FindArchiveItemTagOutput,
    private val saveArchiveItemTagOutput: SaveArchiveItemTagOutput,
    private val findArchiveItemOutput: FindArchiveItemOutput,
    private val writeTransactionTemplate: TransactionTemplate,
) : GetArchiveTagsUseCase,
    ManageArchiveTagsUseCase {

    private val logger = KotlinLogging.logger {}

    override fun getAllTags(): List<ArchiveTag> {
        return findArchiveTagOutput.findAll()
    }

    override fun getTagsByArchiveItem(itemIdentifier: UUID): List<ArchiveTag> {
        return findArchiveItemTagOutput.findTagsByArchiveItemIdentifier(itemIdentifier)
    }

    override fun getArchiveItemsByTag(tagName: String): List<UUID> {
        return findArchiveItemTagOutput.findArchiveItemsByTagName(tagName)
    }

    override fun addTagsToArchiveItem(itemIdentifier: UUID, tagNames: List<String>): List<ArchiveTag> {
        logger.info { "Adding tags to archive item: $itemIdentifier, tags: $tagNames" }

        return writeTransactionTemplate.execute {
            // 아이템 존재 확인
            findArchiveItemOutput.findArchiveItemById(itemIdentifier)
                ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")

            // 기존 태그들과 새로운 태그들 처리
            val existingTags = findArchiveTagOutput.findAllByNameInAndTypeIn(tagNames, listOf("user"))
            val existingTagNames = existingTags.map { it.name }.toSet()

            // 새로운 태그들 생성
            val newTagNames = tagNames.filter { it !in existingTagNames }
            val newTags = newTagNames.map { ArchiveTag.create(it, "user") }
            val savedNewTags = if (newTags.isNotEmpty()) {
                saveArchiveTagOutput.saveAllTags(newTags)
            } else {
                emptyList()
            }

            // 모든 태그들
            val allTags = existingTags + savedNewTags

            // 기존 아이템-태그 연결 확인
            val existingItemTags = findArchiveItemTagOutput.findByArchiveItemIdentifier(itemIdentifier)
            val existingTagIds = existingItemTags.map { it.tagIdentifier }.toSet()

            // 새로운 연결만 생성
            val newItemTags = allTags
                .filter { it.identifier !in existingTagIds }
                .map { ArchiveItemTag.create(itemIdentifier, it.identifier) }

            if (newItemTags.isNotEmpty()) {
                saveArchiveItemTagOutput.saveAllItemTags(newItemTags)
            }

            allTags
        } ?: throw RuntimeException("Failed to add tags to archive item")
    }

    override fun removeTagsFromArchiveItem(itemIdentifier: UUID, tagNames: List<String>) {
        logger.info { "Removing tags from archive item: $itemIdentifier, tags: $tagNames" }

        writeTransactionTemplate.execute {
            // 아이템 존재 확인
            findArchiveItemOutput.findArchiveItemById(itemIdentifier)
                ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")

            // 제거할 태그들 찾기
            val tagsToRemove = findArchiveTagOutput.findAllByNameInAndTypeIn(tagNames, listOf("user"))
            val tagIdsToRemove = tagsToRemove.map { it.identifier }.toSet()

            // 기존 아이템-태그 연결에서 해당 태그들 제거
            val existingItemTags = findArchiveItemTagOutput.findByArchiveItemIdentifier(itemIdentifier)
            val itemTagsToRemove = existingItemTags.filter { it.tagIdentifier in tagIdsToRemove }

            // TODO: 개별 삭제 메서드 구현 필요
            // 현재는 전체 삭제 후 재생성으로 처리
            saveArchiveItemTagOutput.deleteByArchiveItemIdentifier(itemIdentifier)

            val remainingItemTags = existingItemTags.filter { it.tagIdentifier !in tagIdsToRemove }
            if (remainingItemTags.isNotEmpty()) {
                saveArchiveItemTagOutput.saveAllItemTags(remainingItemTags)
            }
        }
    }

    override fun updateArchiveItemTags(itemIdentifier: UUID, tagNames: List<String>): List<ArchiveTag> {
        logger.info { "Updating tags for archive item: $itemIdentifier, tags: $tagNames" }

        return writeTransactionTemplate.execute {
            // 아이템 존재 확인
            findArchiveItemOutput.findArchiveItemById(itemIdentifier)
                ?: throw IllegalArgumentException("Archive item not found: $itemIdentifier")

            // 기존 태그 연결 모두 삭제
            saveArchiveItemTagOutput.deleteByArchiveItemIdentifier(itemIdentifier)

            // 새로운 태그들로 설정
            if (tagNames.isNotEmpty()) {
                addTagsToArchiveItem(itemIdentifier, tagNames)
            } else {
                emptyList()
            }
        } ?: throw RuntimeException("Failed to update archive item tags")
    }
}
