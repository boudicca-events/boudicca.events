plugins {
    id("boudicca-springboot-app")
}

dependencies {
    api("com.github.jknack:handlebars:4.4.0")
    api(project(":boudicca.base:search-client"))
    testImplementation("com.microsoft.playwright:playwright:1.46.0")
    testImplementation("com.deque.html.axe-core:playwright:4.9.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}
