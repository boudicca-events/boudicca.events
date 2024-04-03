plugins {
    id("boudicca-springboot-app")
}

dependencies {
    api("com.github.jknack:handlebars:4.4.0")
    api(project(":boudicca.base:search-client"))

    api("com.microsoft.playwright:playwright:1.42.0")
    api("com.deque.html.axe-core:playwright:4.8.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}
