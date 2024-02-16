plugins {
    id("boudicca-kotlin")
}

dependencies {
    implementation("org.json:json:20240205")
    implementation(project(":boudicca.base:enricher-client"))
    implementation(project(":boudicca.base:publisher-client"))
}
