plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:common-model"))
    api(project(":boudicca.base:fetcher-lib"))
    api(libs.biweekly)
    implementation(project(":boudicca.base:publisher-client"))
    implementation(project(":boudicca.base:ingest-client"))
    implementation(project(":boudicca.base:enricher-client"))
    implementation(project(":boudicca.base:remote-collector:remote-collector-client"))
    implementation(libs.apache.velocity.engine.core)
    implementation(libs.apache.velocity.tools.generic)
    implementation(libs.logback)
    implementation(libs.slf4j)
}
