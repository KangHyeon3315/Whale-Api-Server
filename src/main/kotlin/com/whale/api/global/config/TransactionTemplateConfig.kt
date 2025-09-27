package com.whale.api.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Configuration
class TransactionTemplateConfig(
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean("readTransactionTemplate")
    fun readOnlyTransactionTemplate(): TransactionTemplate {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isReadOnly = true
        return transactionTemplate
    }

    @Bean("writeTransactionTemplate")
    fun writeTransactionTemplate(): TransactionTemplate {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isReadOnly = false
        return transactionTemplate
    }
}
