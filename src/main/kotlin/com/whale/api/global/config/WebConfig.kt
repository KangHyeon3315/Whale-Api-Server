package com.whale.api.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "https://whale-secretary.kro.kr/",
            )
            .allowedOriginPatterns(
                "http://192.168.0.*:*",
                "http://localhost:*",
            )
            .allowedMethods(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.HEAD.name(),
            )
    }

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        // REST API 경로가 정적 리소스로 인식되지 않도록 설정
        configurer.setUseTrailingSlashMatch(false)
    }
}
