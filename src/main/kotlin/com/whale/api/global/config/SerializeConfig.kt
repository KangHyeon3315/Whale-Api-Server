package com.whale.api.global.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Configuration
class SerializeConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
            .registerModule(
                JavaTimeModule()
                    .addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ISO_DATE))
                    .addSerializer(OffsetDateTime::class.java, offsetDateTimeSerializer()),
            )
            .registerModule(kotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
    }

    private fun offsetDateTimeSerializer(): JsonSerializer<OffsetDateTime> {
        return object : StdSerializer<OffsetDateTime>(OffsetDateTime::class.java) {
            override fun serialize(
                value: OffsetDateTime?,
                gen: JsonGenerator?,
                provider: SerializerProvider?,
            ) {
                gen?.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value))
            }
        }
    }
}
