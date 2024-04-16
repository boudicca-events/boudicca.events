plugins {
    id("boudicca-springboot-app")
}

dependencies {
    api("com.github.jknack:handlebars:4.4.0")
    api(project(":boudicca.base:search-client"))
    testImplementation("com.microsoft.playwright:playwright:1.42.0")
    testImplementation("com.deque.html.axe-core:playwright:4.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}
