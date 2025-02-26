/**
 * default spring boot application with an openapi restapi with docker "imageBuild" task
 */

plugins {
    id("boudicca-springboot-app")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    api(versionCatalog.findLibrary("spring-boot-starter-web").get())
    api(versionCatalog.findLibrary("spring-openapi-starter").get())
    api(versionCatalog.findLibrary("jackson-module-kotlin").get())
}
