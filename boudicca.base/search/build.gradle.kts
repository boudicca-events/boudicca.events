plugins {
    id("boudicca-springboot-rest-app")
}

dependencies {
    implementation(project(":boudicca.base:common-model"))
    implementation(project(":boudicca.base:search-api"))
    implementation(project(":boudicca.base:publisher-client"))
    implementation(project(":boudicca.base:query-lib"))
}
