plugins {
    id("boudicca-springboot-app")
}

dependencies {
    api(project(":boudicca.base:search-client"))
    api("com.github.jknack:handlebars:4.4.0")
    testImplementation("com.microsoft.playwright:playwright:1.49.0")
    testImplementation("com.deque.html.axe-core:playwright:4.10.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
//TODO make this use version catalogue
//    api(libs.handlebars)
//    testImplementation(libs.mockito.kotlin)
//    testImplementation(libs.microsoft.playwright)
//    testImplementation(libs.axe.core.playwright)
}
