plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
    alias(libs.plugins.jmh)
}

dependencies {
    api(project(":boudicca.base:common-model"))
    jmh(project(":boudicca.base:publisher-client"))
    jmh(project(":boudicca.base:ingest-client"))
    jmh(libs.jackson.core)
    jmh(libs.jackson.module.kotlin)
    jmh(libs.jackson.databind.jsr310)
    jmh(libs.jackson.databind)
}

tasks.named("assemble") {
    dependsOn(tasks.named("jmhClasses"))
}
