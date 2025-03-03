plugins {
    id("boudicca-springboot-rest-lib")
}

dependencies {
    api(project(":boudicca.base:enricher-api"))
    api(project(":boudicca.base:common-model"))
}
