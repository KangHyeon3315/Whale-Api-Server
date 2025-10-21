package com.whale.api.email.application.port.out

import com.whale.api.email.domain.EmailAccount

interface SaveEmailAccountOutput {
    fun save(emailAccount: EmailAccount): EmailAccount
}
