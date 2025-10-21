package com.whale.api.email.application.port.out

import com.whale.api.email.domain.EmailAttachment

interface SaveEmailAttachmentOutput {
    fun save(emailAttachment: EmailAttachment): EmailAttachment
    fun saveAll(emailAttachments: List<EmailAttachment>): List<EmailAttachment>
}
