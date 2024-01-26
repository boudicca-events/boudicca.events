plugins {
    id("boudicca-kotlin")
}

dependencies {
    api(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:search-openapi"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}
