package com.whale.api.global.utils

import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.ComparablePath
import com.querydsl.core.types.dsl.DateTimePath
import java.time.OffsetDateTime

object QuerydslUtils {
    fun <T : Comparable<T>> greaterThanIfNotNull(
        value: T?,
        comparablePath: ComparablePath<T>,
    ): Predicate? {
        return if (value == null) {
            null
        } else {
            comparablePath.gt(value)
        }
    }

    fun greaterThanIfNotNull(
        value: OffsetDateTime?,
        dateTimePath: DateTimePath<OffsetDateTime>,
    ): Predicate? {
        return if (value == null) {
            null
        } else {
            dateTimePath.gt(value)
        }
    }

    fun lessThanIfNotNull(
        value: OffsetDateTime?,
        dateTimePath: DateTimePath<OffsetDateTime>,
    ): Predicate? {
        return if (value == null) {
            null
        } else {
            dateTimePath.lt(value)
        }
    }

    fun <T : Comparable<T>> lessThanIfNotNull(
        value: T?,
        comparablePath: ComparablePath<T>,
    ): Predicate? {
        return if (value == null) {
            null
        } else {
            comparablePath.lt(value)
        }
    }
}
