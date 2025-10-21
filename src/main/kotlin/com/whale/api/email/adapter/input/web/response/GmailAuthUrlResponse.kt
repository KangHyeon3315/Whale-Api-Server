package com.whale.api.email.adapter.input.web.response

data class GmailAuthUrlResponse(
    val authUrl: String,
    val state: String,
) {
    companion object {
        fun from(authUrl: String, userId: String): GmailAuthUrlResponse {
            return GmailAuthUrlResponse(
                authUrl = authUrl,
                state = userId,
            )
        }
    }
}
