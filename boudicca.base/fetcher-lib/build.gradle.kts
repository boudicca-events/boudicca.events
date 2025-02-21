plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:common-model"))
    implementation(libs.logback)
    implementation(libs.slf4j)
}
