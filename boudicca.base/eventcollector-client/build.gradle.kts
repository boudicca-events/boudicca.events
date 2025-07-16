plugins {
    id("boudicca-springboot-lib")
}

dependencies {
    api(project(":boudicca.base:common-model"))
    api(project(":boudicca.base:fetcher-lib"))
    api(project(":boudicca.base:dateparser-lib"))
    api(libs.biweekly)
    api(libs.spring.boot.starter.web)
    api(libs.handlebars)
    api(libs.handlebars.springmvc)
    implementation(project(":boudicca.base:publisher-client"))
    implementation(project(":boudicca.base:ingest-client"))
    implementation(project(":boudicca.base:enricher-client"))
    implementation(project(":boudicca.base:remote-collector:remote-collector-client"))
}
