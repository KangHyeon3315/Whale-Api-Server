package com.whale.api.archive.application

import com.whale.api.archive.application.port.`in`.CreateArchiveUseCase
import com.whale.api.archive.application.port.`in`.GetArchiveStatusUseCase
import com.whale.api.archive.application.port.`in`.StartArchiveUseCase
import com.whale.api.archive.application.port.`in`.command.CreateArchiveCommand
import com.whale.api.archive.application.port.out.FindArchiveOutput
import com.whale.api.archive.application.port.out.SaveArchiveOutput
import com.whale.api.archive.domain.Archive
import com.whale.api.archive.domain.enums.ArchiveStatus
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
    GetArchiveStatusUseCase,
    StartArchiveUseCase {

    private val logger = KotlinLogging.logger {}

    override fun createArchive(command: CreateArchiveCommand): Archive {
        logger.info { "Creating new archive: ${command.name}" }

        val archive = Archive(
            identifier = UUID.randomUUID(),
            name = command.name,
            description = command.description,
            status = ArchiveStatus.PENDING,
            totalItems = 0,
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

    override fun startArchive(archiveIdentifier: UUID) {
        logger.info { "Starting archive: $archiveIdentifier" }

        writeTransactionTemplate.execute {
            val archive = findArchiveOutput.findById(archiveIdentifier)
                ?: throw IllegalArgumentException("Archive not found: $archiveIdentifier")

            if (!archive.canStart()) {
                throw IllegalStateException("Archive cannot be started. Current status: ${archive.status}")
            }

            archive.start()
            saveArchiveOutput.save(archive)
        }
    }

    override fun getArchive(archiveIdentifier: UUID): Archive {
        return findArchiveOutput.findById(archiveIdentifier)
            ?: throw IllegalArgumentException("Archive not found: $archiveIdentifier")
    }

    override fun getAllArchives(): List<Archive> {
        return findArchiveOutput.findAll()
    }
}
