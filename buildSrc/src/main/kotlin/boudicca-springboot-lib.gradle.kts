plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")

    kotlin("plugin.spring")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    testImplementation(versionCatalog.findLibrary("spring-boot-starter-test").get())
}
