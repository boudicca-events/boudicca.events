import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.cloudflight.autoconfigure-gradle") version "1.0.0"

    kotlin("jvm") version "1.9.10" apply false
    kotlin("plugin.allopen") version "1.9.10" apply false
    kotlin("plugin.spring") version "1.9.10" apply false
    id("org.springframework.boot") version "3.1.5" apply false
    id("io.spring.dependency-management") version "1.1.3" apply false
    id("io.cloudflight.autoconfigure.swagger-api-configure") version "1.0.2" apply false
}