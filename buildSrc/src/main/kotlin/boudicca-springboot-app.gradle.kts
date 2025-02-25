/**
 * default spring boot application with docker "imageBuild" task
 * TODO split up between openapi services and the html publishers
 */

plugins {
    id("boudicca-kotlin")
    id("boudicca-docker")

    id("org.springframework.boot")
    kotlin("plugin.spring")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    api(versionCatalog.findBundle("springboot-app").get())

    developmentOnly(versionCatalog.findLibrary("spring-boot-devtools").get())
    testImplementation(versionCatalog.findLibrary("spring-boot-starter-test").get())
    testImplementation(versionCatalog.findLibrary("spring-mockk").get())
}
