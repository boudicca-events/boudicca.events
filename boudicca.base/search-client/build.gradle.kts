plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:semantic-conventions"))
    api(project(":boudicca.base:query-lib"))
    implementation(project(":boudicca.base:search-openapi"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}
