/**
 * default spring boot application with docker "imageBuild" task
 */

plugins {
    id("boudicca-kotlin")
    id("boudicca-docker")

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    api(versionCatalog.findBundle("springboot-app").get())

    developmentOnly(versionCatalog.findLibrary("spring-boot-devtools").get())
    testImplementation(versionCatalog.findLibrary("spring-boot-starter-test").get())
    testImplementation(versionCatalog.findLibrary("spring-mockk").get())
}
