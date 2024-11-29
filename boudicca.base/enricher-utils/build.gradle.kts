plugins {
    id("boudicca-kotlin")
}

dependencies {
    implementation(libs.json)
    implementation(project(":boudicca.base:enricher-client"))
    implementation(project(":boudicca.base:publisher-client"))
}
