plugins {
    id("boudicca-kotlin")
}

dependencies {
    implementation("org.json:json:20240303")
    implementation(project(":boudicca.base:enricher-client"))
    implementation(project(":boudicca.base:publisher-client"))
}
