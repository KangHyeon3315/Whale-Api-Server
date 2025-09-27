import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "2.0.21"
    val springBootVersion = "3.4.0"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("kapt") version kotlinVersion

    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jmailen.kotlinter") version "5.0.0"
}

group = "com.whale"
version = "0.0.1-SNAPSHOT"
description = "whale-api-server"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")

    // Hibernate Spatial
    implementation("com.querydsl:querydsl-spatial")
    implementation("org.hibernate.orm:hibernate-spatial")
    implementation("org.locationtech.jts:jts-core:1.19.0")

    // Logging & Observability
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    implementation("org.flywaydb:flyway-core")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Metrics
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("io.mockk:mockk-jvm:1.13.12")
    testImplementation("com.appmattus.fixture:fixture:1.2.0")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/source/kapt/main")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-opt-in=kotlin.time.ExperimentalTime",
        )
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "-XX:+UnlockExperimentalVMOptions"
    )
}

tasks.register("unitTest", Test::class) {
    useJUnitPlatform {
        includeTags("unit")
        includeTags("parameterizedUnit")
    }
}

tasks.register("integrationTest", Test::class) {
    useJUnitPlatform {
        includeTags("integration")
    }
}

// Kotlinter 설정 (KtLint 기반) - Kotlin 2.0.21에서 안정적으로 작동
kotlinter {
    ktlintVersion = "1.0.1"
    ignoreLintFailures = false
    ignoreFormatFailures = false
    reporters = arrayOf("checkstyle", "plain")
}

// KAPT와 Kotlinter 간 의존성 설정
tasks.named("lintKotlinMain") {
    dependsOn("kaptKotlin")
}

tasks.named("lintKotlinTest") {
    dependsOn("kaptTestKotlin")
}

tasks.named("formatKotlinMain") {
    dependsOn("kaptKotlin")
}

tasks.named("formatKotlinTest") {
    dependsOn("kaptTestKotlin")
}

tasks.wrapper {
    gradleVersion = "8.11"
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveClassifier = ""
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Built-JDK" to System.getProperty("java.version"),
            "Virtual-Threads-Enabled" to "true"
        )
    }
}
