/**
 * plugin for springboot "libraries", meaning they cannot run themselves but are supposed to be used in another project
 */

plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")

    kotlin("plugin.spring")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    testImplementation(versionCatalog.findLibrary("spring-boot-starter-test").get())
    testImplementation(versionCatalog.findLibrary("spring-mockk").get())
}
