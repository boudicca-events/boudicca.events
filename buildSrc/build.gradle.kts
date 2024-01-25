plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("io.cloudflight.autoconfigure.swagger-api-configure:io.cloudflight.autoconfigure.swagger-api-configure.gradle.plugin:1.1.1")
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.9.20")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:1.9.20")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:3.2.1")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.4")
}
