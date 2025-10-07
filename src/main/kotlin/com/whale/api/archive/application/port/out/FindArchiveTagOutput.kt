package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.ArchiveTag
import java.util.UUID

interface FindArchiveTagOutput {
    fun findByName(name: String): ArchiveTag?
    fun findByNameAndType(name: String, type: String): ArchiveTag?
    fun findAllByNameInAndTypeIn(names: List<String>, types: List<String>): List<ArchiveTag>
    fun findAll(): List<ArchiveTag>
    fun findById(identifier: UUID): ArchiveTag?
}
