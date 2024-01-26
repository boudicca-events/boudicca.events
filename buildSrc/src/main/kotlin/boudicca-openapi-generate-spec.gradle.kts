plugins {
    kotlin("kapt")
    id("io.cloudflight.autoconfigure.swagger-api-configure")
    id("boudicca-kotlin")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(platform(versionCatalog.findLibrary("cloudflight.platform.spring.bom").get()))
    implementation(versionCatalog.findBundle("openapi-generate-spec").get())
}