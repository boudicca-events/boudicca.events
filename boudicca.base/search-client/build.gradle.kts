plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:common-model"))
    api(project(":boudicca.base:query-lib"))
    implementation(project(":boudicca.base:search-openapi"))
}
