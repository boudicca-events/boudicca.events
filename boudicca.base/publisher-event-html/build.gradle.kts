plugins {
    id("boudicca-springboot-app")
}

dependencies {
    api("com.github.jknack:handlebars:4.4.0")
    api(project(":boudicca.base:search-client"))
    testImplementation("com.microsoft.playwright:playwright:1.45.1")
    testImplementation("com.deque.html.axe-core:playwright:4.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
}
