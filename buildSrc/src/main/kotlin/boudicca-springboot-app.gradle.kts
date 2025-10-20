/**
 * default spring boot application with docker "imageBuild" task, has no starters except testing ones and the devtools
 */

plugins {
    id("boudicca-springboot-app-nodevtools")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    developmentOnly(versionCatalog.findLibrary("spring-boot-devtools").get())
}

tasks.named<Jar>("jar") {
    enabled = false
}
