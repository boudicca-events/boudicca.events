/**
 * plugin for springboot "libraries" with an openapi rest api
 */

plugins {
    id("boudicca-springboot-lib")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    api(versionCatalog.findLibrary("spring-boot-starter-web").get())
    api(versionCatalog.findLibrary("spring-openapi-starter").get())
    api(versionCatalog.findLibrary("jackson-module-kotlin").get())
}
