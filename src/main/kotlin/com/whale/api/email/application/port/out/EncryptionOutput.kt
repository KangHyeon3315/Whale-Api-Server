package com.whale.api.email.application.port.out

interface EncryptionOutput {
    fun encrypt(plainText: String): String
    
    fun decrypt(encryptedText: String): String
}
