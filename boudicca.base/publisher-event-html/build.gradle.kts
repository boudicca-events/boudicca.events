plugins {
    id("boudicca-springboot-lib")
}

dependencies {
    api(project(":boudicca.base:search-client"))
    api(libs.spring.boot.starter.web)
    api(libs.handlebars)
    api(libs.handlebars.springmvc)
    testImplementation(libs.microsoft.playwright)
    testImplementation(libs.axe.core.playwright)
}
