package com.whale.api.file.adapter.output.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "file_hash")
data class FileHashEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @ManyToOne
    @JoinColumn(name = "file_identifier", nullable = false)
    val file: FileEntity,
    @Column(name = "hash", nullable = false)
    val hash: String,
)
