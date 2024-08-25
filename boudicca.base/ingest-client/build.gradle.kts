plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:common-model"))
    implementation(project(":boudicca.base:eventdb-openapi"))
}