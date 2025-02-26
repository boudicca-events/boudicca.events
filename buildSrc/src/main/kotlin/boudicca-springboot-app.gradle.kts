/**
 * default spring boot application with docker "imageBuild" task, has no starters except testing ones
 */

plugins {
    id("boudicca-kotlin")
    id("boudicca-docker")

    id("org.springframework.boot")
    kotlin("plugin.spring")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    implementation(versionCatalog.findLibrary("kotlin-reflect").get())
    developmentOnly(versionCatalog.findLibrary("spring-boot-devtools").get())
    testImplementation(versionCatalog.findLibrary("spring-boot-starter-test").get())
    testImplementation(versionCatalog.findLibrary("spring-mockk").get())
}
