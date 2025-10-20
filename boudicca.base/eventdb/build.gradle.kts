plugins {
    id("boudicca-springboot-rest-lib")
}


dependencies {
    api(project(":boudicca.base:entrydb"))
    testImplementation(libs.spring.security.test)
}
