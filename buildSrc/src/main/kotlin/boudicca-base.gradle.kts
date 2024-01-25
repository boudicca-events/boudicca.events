plugins {
    id("boudicca-base")
}

group = "base.boudicca"
version = "0.1.0-SNAPSHOT"
description = "Boudicca Events"

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}