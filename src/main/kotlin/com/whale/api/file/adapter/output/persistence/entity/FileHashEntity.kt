package com.whale.api.file.adapter.output.persistence.entity

import com.whale.api.file.domain.FileHash
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "file_hash")
data class FileHashEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "file_identifier", nullable = false)
    val fileIdentifier: UUID,
    @Column(name = "hash", nullable = false)
    val hash: String,
) {
    fun toDomain(): FileHash {
        return FileHash(
            identifier = this.identifier,
            fileIdentifier = this.fileIdentifier,
            hash = this.hash,
        )
    }

    companion object {
        fun FileHash.toEntity(): FileHashEntity {
            return FileHashEntity(
                identifier = this.identifier,
                fileIdentifier = this.fileIdentifier,
                hash = this.hash,
            )
        }
    }
}
