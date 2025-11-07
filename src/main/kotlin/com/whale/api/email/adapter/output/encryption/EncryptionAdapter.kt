package com.whale.api.email.adapter.output.encryption

import com.whale.api.email.application.port.out.EncryptionOutput
import com.whale.api.email.domain.property.EmailProperty
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class EncryptionAdapter(
    private val emailProperty: EmailProperty,
) : EncryptionOutput {
    private val textEncryptor: TextEncryptor by lazy {
        val password = emailProperty.encryptionSecretKey
        val salt = generateSalt()
        Encryptors.text(password, salt)
    }

    override fun encrypt(plainText: String): String {
        return try {
            textEncryptor.encrypt(plainText)
        } catch (e: Exception) {
            throw EncryptionException("Failed to encrypt text", e)
        }
    }

    override fun decrypt(encryptedText: String): String {
        return try {
            textEncryptor.decrypt(encryptedText)
        } catch (e: Exception) {
            throw EncryptionException("Failed to decrypt text", e)
        }
    }

    private fun generateSalt(): String {
        // 고정된 salt를 사용하여 동일한 암호화 결과를 보장
        // 실제 운영환경에서는 더 안전한 방법을 고려해야 함
        val secretKey = emailProperty.encryptionSecretKey
        val saltBytes = secretKey.take(16).padEnd(16, '0').toByteArray()
        return String(Base64.getEncoder().encode(saltBytes))
    }
}

class EncryptionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
