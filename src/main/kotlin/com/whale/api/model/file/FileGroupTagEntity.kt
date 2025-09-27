package com.whale.api.model.file

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "file_group_tag")
data class FileGroupTagEntity(
    @Id
    @Column(name = "identifier", nullable = false)
    val identifier: UUID,
    @ManyToOne
    @JoinColumn(name = "file_group_identifier", nullable = false)
    val fileGroup: FileGroupEntity,
    @ManyToOne
    @JoinColumn(name = "tag_identifier", nullable = false)
    val tag: TagEntity,
)
