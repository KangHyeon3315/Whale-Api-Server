package com.whale.api.archive.application

import com.whale.api.archive.application.port.`in`.CreateArchiveUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveStatusUseCase
import com.whale.api.archive.application.port.`in`.command.CreateArchiveCommand
import com.whale.api.archive.application.port.out.FindArchiveOutput
import com.whale.api.archive.application.port.out.SaveArchiveOutput
import com.whale.api.archive.domain.Archive
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ArchiveService(
    private val saveArchiveOutput: SaveArchiveOutput,
    private val findArchiveOutput: FindArchiveOutput,
    private val writeTransactionTemplate: TransactionTemplate,
) : CreateArchiveUseCase,
    GetArchiveStatusUseCase {
    private val logger = KotlinLogging.logger {}

    override fun createArchive(command: CreateArchiveCommand): Archive {
        logger.info { "Creating new archive: ${command.name}" }

        val archive =
            Archive(
                identifier = UUID.randomUUID(),
                name = command.name,
                description = command.description,
                totalItems = command.totalItems,
                processedItems = 0,
                failedItems = 0,
                createdDate = OffsetDateTime.now(),
                modifiedDate = OffsetDateTime.now(),
                completedDate = null,
            )

        return writeTransactionTemplate.execute {
            saveArchiveOutput.save(archive)
        } ?: throw RuntimeException("Failed to create archive")
    }

    override fun getArchive(archiveIdentifier: UUID): Archive {
        return findArchiveOutput.findArchiveById(archiveIdentifier)
            ?: throw IllegalArgumentException("Archive not found: $archiveIdentifier")
    }

    override fun getAllArchives(): List<Archive> {
        return findArchiveOutput.findAll()
    }
}
