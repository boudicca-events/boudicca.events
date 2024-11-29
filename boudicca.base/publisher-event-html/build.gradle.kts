plugins {
    id("boudicca-springboot-app")
}

dependencies {
    api(project(":boudicca.base:search-client"))
    api(libs.handlebars)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.microsoft.playwright)
    testImplementation(libs.axe.core.playwright)
}
