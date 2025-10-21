package com.whale.api.email.adapter.input.web.response

import com.whale.api.email.domain.Email

data class EmailListResponse(
    val emails: List<EmailResponse>,
    val totalCount: Int,
    val hasNext: Boolean,
) {
    companion object {
        fun from(
            emails: List<Email>,
            totalCount: Int = emails.size,
            hasNext: Boolean = false,
        ): EmailListResponse {
            return EmailListResponse(
                emails = EmailResponse.fromList(emails),
                totalCount = totalCount,
                hasNext = hasNext,
            )
        }
    }
}
