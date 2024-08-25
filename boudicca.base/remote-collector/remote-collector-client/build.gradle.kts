plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:common-model"))
    api(project(":boudicca.base:remote-collector:remote-collector-api"))
    implementation(project(":boudicca.base:remote-collector:remote-collector-openapi"))
}
