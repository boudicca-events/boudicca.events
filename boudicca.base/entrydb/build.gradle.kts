plugins {
    id("boudicca-springboot-rest-lib")
}

dependencies {
    implementation(libs.spring.boot.starter.security)
    api(project(":boudicca.base:common-model"))
    api(project(":boudicca.base:eventdb-api"))
    testImplementation(libs.spring.security.test)
}
