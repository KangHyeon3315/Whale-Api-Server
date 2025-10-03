package com.whale.api.service.file.util

import com.whale.api.controller.file.response.TagResponse
import com.whale.api.model.file.*
import com.whale.api.model.taskqueue.dto.TagDto
import com.whale.api.file.adapter.output.persistence.repository.FileTagRepository
import com.whale.api.file.adapter.output.persistence.repository.FileGroupTagRepository
import com.whale.api.file.adapter.output.persistence.repository.TagRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class TagUtil(
    private val tagRepository: TagRepository,
    private val fileTagRepository: FileTagRepository,
    private val fileGroupTagRepository: FileGroupTagRepository
) {

    fun createOrGetTag(tagDto: TagDto): TagEntity {
        return tagRepository.findByName(tagDto.name) ?: run {
            val newTag = TagEntity(
                identifier = UUID.randomUUID(),
                name = tagDto.name,
                type = tagDto.type
            )
            tagRepository.save(newTag)
        }
    }

    fun createFileTag(file: FileEntity, tag: TagEntity): FileTagEntity {
        return FileTagEntity(
            identifier = UUID.randomUUID(),
            file = file,
            tag = tag
        )
    }

    fun createFileGroupTag(fileGroup: FileGroupEntity, tag: TagEntity): FileGroupTagEntity {
        return FileGroupTagEntity(
            identifier = UUID.randomUUID(),
            fileGroup = fileGroup,
            tag = tag
        )
    }

    fun getFileTagResponses(fileIdentifiers: List<UUID>): Map<UUID, List<TagResponse>> {
        val fileTags = fileTagRepository.findByFileIdentifiersWithTag(fileIdentifiers)
        return fileTags.groupBy { it.file.identifier }
            .mapValues { (_, tags) ->
                tags.map { fileTag ->
                    TagResponse(
                        identifier = fileTag.tag.identifier,
                        name = fileTag.tag.name,
                        type = fileTag.tag.type
                    )
                }
            }
    }

    fun getFileGroupTagResponses(fileGroupIdentifiers: List<UUID>): Map<UUID, List<TagResponse>> {
        val fileGroupTags = fileGroupTagRepository.findByFileGroupIdentifiersWithTag(fileGroupIdentifiers)
        return fileGroupTags.groupBy { it.fileGroup.identifier }
            .mapValues { (_, tags) ->
                tags.map { fileGroupTag ->
                    TagResponse(
                        identifier = fileGroupTag.tag.identifier,
                        name = fileGroupTag.tag.name,
                        type = fileGroupTag.tag.type
                    )
                }
            }
    }

    fun updateFileTags(file: FileEntity, tagIdentifiers: List<UUID>) {
        // 기존 태그 삭제
        fileTagRepository.deleteByFileIdentifier(file.identifier)

        // 새 태그 추가
        val newFileTags = tagIdentifiers.mapNotNull { tagIdentifier ->
            tagRepository.findById(tagIdentifier).orElse(null)?.let { tag ->
                createFileTag(file, tag)
            }
        }

        fileTagRepository.saveAll(newFileTags)
    }

    fun updateFileGroupTags(fileGroup: FileGroupEntity, tagIdentifiers: List<UUID>) {
        // 기존 태그 삭제
        fileGroupTagRepository.deleteByFileGroupIdentifier(fileGroup.identifier)

        // 새 태그 추가
        val newFileGroupTags = tagIdentifiers.mapNotNull { tagIdentifier ->
            tagRepository.findById(tagIdentifier).orElse(null)?.let { tag ->
                createFileGroupTag(fileGroup, tag)
            }
        }

        fileGroupTagRepository.saveAll(newFileGroupTags)
    }
}
