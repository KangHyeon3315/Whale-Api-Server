package com.whale.api.file.adapter.output.persistence.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.whale.api.file.adapter.output.persistence.entity.SaveTaskEntity.TagRequestEntity
import com.whale.api.file.domain.SaveTask
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component

@Converter
@Component
class TagRequestEntityConverter(
    private val objectMapper: ObjectMapper,
) : AttributeConverter<List<TagRequestEntity>, String> {
    override fun convertToDatabaseColumn(attribute: List<TagRequestEntity>?): String? {
        return if (attribute.isNullOrEmpty()) {
            "[]"
        } else {
            try {
                objectMapper.writeValueAsString(attribute)
            } catch (e: Exception) {
                throw RuntimeException("Failed to convert TagRequestEntity list to JSON", e)
            }
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<TagRequestEntity> {
        return if (dbData.isNullOrBlank() || dbData == "[]") {
            emptyList()
        } else {
            try {
                objectMapper.readValue(dbData, object : TypeReference<List<TagRequestEntity>>() {})
            } catch (e: Exception) {
                throw RuntimeException("Failed to convert JSON to TagRequestEntity list", e)
            }
        }
    }

    companion object {
        /**
         * SaveTask.Tag 도메인 객체를 TagRequestEntity로 변환
         */
        fun SaveTask.Tag.toTagRequestEntity(): TagRequestEntity {
            return TagRequestEntity(
                name = this.name,
                type = this.type,
            )
        }

        /**
         * TagRequestEntity를 SaveTask.Tag 도메인 객체로 변환
         */
        fun TagRequestEntity.toSaveTaskTag(): SaveTask.Tag {
            return SaveTask.Tag(
                name = this.name,
                type = this.type,
            )
        }

        /**
         * SaveTask.Tag 리스트를 TagRequestEntity 리스트로 변환
         */
        fun List<SaveTask.Tag>.toTagRequestEntities(): List<TagRequestEntity> {
            return this.map { it.toTagRequestEntity() }
        }

        /**
         * TagRequestEntity 리스트를 SaveTask.Tag 리스트로 변환
         */
        fun List<TagRequestEntity>.toSaveTaskTags(): List<SaveTask.Tag> {
            return this.map { it.toSaveTaskTag() }
        }
    }
}
