plugins {
    id("boudicca-springboot-app")
}

dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:search-api"))
    implementation(project(":boudicca.base:publisher-client"))
}
