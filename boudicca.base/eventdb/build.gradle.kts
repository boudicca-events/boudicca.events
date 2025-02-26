plugins {
    id("boudicca-springboot-rest-app")
}


dependencies {
    implementation(libs.spring.boot.starter.security)
    implementation(project(":boudicca.base:common-model"))
    implementation(project(":boudicca.base:eventdb-api"))
    testImplementation(libs.spring.security.test)
}
