package com.whale.api.file.adapter.output.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "file_tag")
data class FileTagEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @ManyToOne
    @JoinColumn(name = "file_identifier", nullable = false)
    val file: FileEntity,
    @ManyToOne
    @JoinColumn(name = "tag_identifier", nullable = false)
    val tag: TagEntity,
)
