package com.whale.api.global.utils

import java.util.Base64

object Encoder {
    fun encodeBase64(value: String): String {
        return Base64.getEncoder().encodeToString(value.toByteArray())
    }

    fun decodeBase64(base64: String): String {
        return Base64.getDecoder().decode(base64).toString(Charsets.UTF_8)
    }
}
