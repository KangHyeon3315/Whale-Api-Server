package com.whale.api.email.application.port.out

import java.util.UUID

interface DeleteEmailAttachmentOutput {
    fun deleteByEmailIdentifier(emailIdentifier: UUID)
}
