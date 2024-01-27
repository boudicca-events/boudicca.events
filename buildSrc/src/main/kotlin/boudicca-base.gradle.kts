/**
 * base plugin which will apply default repos, group, version, ...
 * does not apply java or kotlin
 */

plugins {
    id("boudicca-base")
}

group = "base.boudicca"
version = rootProject.version
description = "Boudicca Events"

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}