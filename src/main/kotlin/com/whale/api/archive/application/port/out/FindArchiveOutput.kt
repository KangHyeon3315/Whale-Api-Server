package com.whale.api.archive.application.port.out

import com.whale.api.archive.domain.Archive
import java.util.UUID

interface FindArchiveOutput {
    fun findById(identifier: UUID): Archive?
    fun findAll(): List<Archive>
}
