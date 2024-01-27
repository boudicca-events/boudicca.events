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