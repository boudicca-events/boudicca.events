plugins {
    id("boudicca-springboot-app")
}

dependencies {
    api(project(":boudicca.base:search-client"))
    api(libs.handlebars)
    api(libs.handlebars.springmvc)
    testImplementation(libs.microsoft.playwright)
    testImplementation(libs.axe.core.playwright)
}
