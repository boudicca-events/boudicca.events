plugins {
    id("boudicca-openapi-generate-client")
}

dependencies {
    openapi(project(":boudicca.base:search-api"))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
