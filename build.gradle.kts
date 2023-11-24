import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("kapt") version "1.9.10"
    kotlin("plugin.allopen") version "1.9.10" apply false
    kotlin("plugin.spring") version "1.9.10" apply false
    id("org.springframework.boot") version "3.1.5" apply false
    id("io.spring.dependency-management") version "1.1.3" apply false
}

allprojects {
    group = "base.boudicca"
    version = "1.0-SNAPSHOT"
    description = "asdf"
    repositories {
        mavenCentral()
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

subprojects.forEach {
    if (!it.name.endsWith("-ui")) {
        dependencies {
            implementation(platform(libs.cloudflight.platform.spring.bom))
            annotationProcessor(platform(libs.cloudflight.platform.spring.bom))
            testImplementation(platform(libs.cloudflight.platform.spring.test.bom))
            kapt(platform(libs.cloudflight.platform.spring.bom))
        }
    }
}
