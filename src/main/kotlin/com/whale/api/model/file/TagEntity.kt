package com.whale.api.model.file

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
)
