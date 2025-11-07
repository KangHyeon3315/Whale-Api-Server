package com.whale.api.email.adapter.output.persistence

import com.whale.api.email.adapter.output.persistence.entity.EmailEntity.Companion.toEntity
import com.whale.api.email.adapter.output.persistence.repository.EmailRepository
import com.whale.api.email.application.port.out.FindEmailOutput
import com.whale.api.email.application.port.out.SaveEmailOutput
import com.whale.api.email.domain.Email
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class EmailPersistenceAdapter(
    private val emailRepository: EmailRepository,
) : SaveEmailOutput,
    FindEmailOutput {
    override fun save(email: Email): Email {
        return emailRepository.save(email.toEntity()).toDomain()
    }

    override fun saveAll(emails: List<Email>): List<Email> {
        val entities = emails.map { it.toEntity() }
        return emailRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findByIdentifier(identifier: UUID): Email? {
        return emailRepository.findById(identifier).orElse(null)?.toDomain()
    }

    override fun findByAccountIdentifier(
        accountIdentifier: UUID,
        folderName: String?,
        isRead: Boolean?,
        limit: Int,
        offset: Int,
    ): List<Email> {
        val pageable =
            PageRequest.of(
                offset / limit,
                limit,
                Sort.by(Sort.Direction.DESC, "dateReceived"),
            )

        return when {
            folderName != null && isRead != null -> {
                emailRepository.findByEmailAccountIdentifierAndFolderNameAndIsRead(
                    accountIdentifier,
                    folderName,
                    isRead,
                    pageable,
                )
            }
            folderName != null -> {
                emailRepository.findByEmailAccountIdentifierAndFolderName(
                    accountIdentifier,
                    folderName,
                    pageable,
                )
            }
            isRead != null -> {
                emailRepository.findByEmailAccountIdentifierAndIsRead(
                    accountIdentifier,
                    isRead,
                    pageable,
                )
            }
            else -> {
                emailRepository.findByEmailAccountIdentifier(accountIdentifier, pageable)
            }
        }.map { it.toDomain() }
    }

    override fun findByMessageId(
        accountIdentifier: UUID,
        messageId: String,
    ): Email? {
        return emailRepository.findByEmailAccountIdentifierAndMessageId(accountIdentifier, messageId)?.toDomain()
    }

    override fun searchEmails(
        accountIdentifier: UUID?,
        query: String,
        limit: Int,
        offset: Int,
    ): List<Email> {
        val pageable = PageRequest.of(offset / limit, limit)
        return emailRepository.searchEmails(accountIdentifier, query, pageable).map { it.toDomain() }
    }

    override fun existsByAccountIdentifierAndMessageId(
        accountIdentifier: UUID,
        messageId: String,
    ): Boolean {
        return emailRepository.existsByEmailAccountIdentifierAndMessageId(accountIdentifier, messageId)
    }
}
