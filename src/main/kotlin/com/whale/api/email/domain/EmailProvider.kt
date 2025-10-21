package com.whale.api.email.domain

enum class EmailProvider(
    val displayName: String,
    val supportsOAuth: Boolean,
    val supportsImap: Boolean,
) {
    GMAIL("Gmail", true, true),
    NAVER("Naver Mail", false, true),
}
