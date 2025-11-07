package com.whale.api.email.application.port.out

import com.whale.api.email.domain.Email

interface SaveEmailOutput {
    fun save(email: Email): Email

    fun saveAll(emails: List<Email>): List<Email>
}
