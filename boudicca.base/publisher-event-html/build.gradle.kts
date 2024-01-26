plugins {
    id("boudicca-springboot-app")
}

dependencies {
    api("com.github.jknack:handlebars:4.3.1")
    api(project(":boudicca.base:search-client"))
}
