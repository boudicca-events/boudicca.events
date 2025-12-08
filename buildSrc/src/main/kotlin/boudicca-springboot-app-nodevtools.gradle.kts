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
    implementation(project(":boudicca.base:common-springboot"))
    implementation(versionCatalog.findLibrary("micrometer-otel-bridge").get())
    implementation(versionCatalog.findLibrary("spring-boot-actuator").get())
    implementation(versionCatalog.findLibrary("kotlin-reflect").get())
    testImplementation(versionCatalog.findLibrary("spring-boot-starter-test").get())
    testImplementation(versionCatalog.findLibrary("spring-boot-starter-webmvc-test").get())
    testImplementation(versionCatalog.findLibrary("spring-mockk").get())
}
