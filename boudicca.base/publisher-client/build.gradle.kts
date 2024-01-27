plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:eventdb-openapi"))
}
