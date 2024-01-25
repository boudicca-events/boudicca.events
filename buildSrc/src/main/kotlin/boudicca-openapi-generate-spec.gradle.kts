plugins {
    kotlin("kapt")
    id("io.cloudflight.autoconfigure.swagger-api-configure")
    id("boudicca-base")
}

dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(platform(versionCatalogs.find("libs").get().findLibrary("cloudflight.platform.spring.bom").get()))
    implementation("io.swagger:swagger-annotations")
    implementation("org.springframework:spring-web")
}