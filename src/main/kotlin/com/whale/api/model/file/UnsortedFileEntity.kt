package com.whale.api.model.file

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "unsorted_file")
data class UnsortedFileEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @Column(name = "path", nullable = false)
    val path: String,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "file_hash", nullable = true, length = 64)
    val fileHash: String?,
    @Column(name = "encoding", nullable = true)
    val encoding: String?,
)
