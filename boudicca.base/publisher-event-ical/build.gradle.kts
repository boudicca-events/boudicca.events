plugins {
    id("boudicca-springboot-rest-app")
}

dependencies {
    implementation(project(":boudicca.base:common-model"))
    implementation(project(":boudicca.base:search-client"))
    implementation(libs.biweekly)
}
