plugins {
    id("boudicca-springboot-app")
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(project(":boudicca.base:common-model"))
    implementation(project(":boudicca.base:eventdb-api"))
    testImplementation("org.springframework.security:spring-security-test")
}