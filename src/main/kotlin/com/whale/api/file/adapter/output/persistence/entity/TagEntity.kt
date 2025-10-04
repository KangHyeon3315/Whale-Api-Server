package com.whale.api.file.adapter.output.persistence.entity

import com.whale.api.file.domain.Tag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tag")
data class TagEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "type", nullable = false)
    val type: String,
) {
    fun toDomain(): Tag {
        return Tag(
            identifier = this.identifier,
            name = this.name,
            type = this.type,
        )
    }

    companion object {
        fun Tag.toEntity(): TagEntity {
            return TagEntity(
                identifier = this.identifier,
                name = this.name,
                type = this.type,
            )
        }
    }
}
