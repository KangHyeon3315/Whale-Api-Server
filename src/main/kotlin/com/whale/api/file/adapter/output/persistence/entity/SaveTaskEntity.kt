package com.whale.api.file.adapter.output.persistence.entity

import com.whale.api.file.adapter.output.persistence.converter.TagRequestEntityConverter
import com.whale.api.file.adapter.output.persistence.converter.TagRequestEntityConverter.Companion.toSaveTaskTags
import com.whale.api.file.adapter.output.persistence.converter.TagRequestEntityConverter.Companion.toTagRequestEntities
import com.whale.api.file.domain.SaveTask
import com.whale.api.file.domain.enums.SaveTaskStatus
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "save_task")
data class SaveTaskEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "file_group_identifier", nullable = true)
    val fileGroupIdentifier: UUID?,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "path", nullable = false)
    val path: String,
    @Column(name = "type", nullable = false)
    val type: String,
    @Column(name = "tag_requests", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = TagRequestEntityConverter::class)
    val tagRequests: List<TagRequestEntity>,
    @Column(name = "sort_order", nullable = true)
    val sortOrder: Int?,
    @Column(name = "status", nullable = false)
    val status: String,
    @Column(name = "created_date", nullable = false)
    val createdDate: OffsetDateTime,
    @Column(name = "modified_date", nullable = false)
    var modifiedDate: OffsetDateTime,
) {
    data class TagRequestEntity(
        val name: String,
        val type: String,
    )

    companion object {
        fun SaveTask.toEntity(): SaveTaskEntity {
            return SaveTaskEntity(
                identifier = this.identifier,
                fileGroupIdentifier = this.fileGroupIdentifier,
                name = this.name,
                path = this.path,
                type = this.type,
                tagRequests = this.tags.toTagRequestEntities(),
                sortOrder = this.sortOrder,
                status = this.status.name,
                createdDate = this.createdDate,
                modifiedDate = this.modifiedDate,
            )
        }
    }

    fun toDomain(): SaveTask {
        return SaveTask(
            identifier = this.identifier,
            fileGroupIdentifier = this.fileGroupIdentifier,
            name = this.name,
            path = this.path,
            type = this.type,
            sortOrder = this.sortOrder,
            tags = this.tagRequests.toSaveTaskTags(),
            status = SaveTaskStatus.valueOf(this.status),
            createdDate = this.createdDate,
            modifiedDate = this.modifiedDate,
        )
    }
}
