plugins {
    id("boudicca-kotlin")
}

dependencies {
    api(project(":boudicca.base:common-model"))
    implementation(libs.logback)
    implementation(libs.slf4j)
    testImplementation(libs.junit.jupiter)
}
