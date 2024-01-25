plugins {
    kotlin("jvm")
}

group = "base.boudicca"
version = "0.1.0-SNAPSHOT"
description = "Boudicca Events"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}