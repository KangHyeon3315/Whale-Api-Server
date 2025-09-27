package com.whale.api.global.annotation

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 인증만 필요하고 특정 역할은 필요하지 않은 엔드포인트에 사용하는 어노테이션
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("isAuthenticated()")
annotation class RequireAuth
