plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
    id ("me.champeau.jmh") version "0.7.2"
}

dependencies {
    api(project(":boudicca.base:common-model"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    jmh(project(":boudicca.base:publisher-client"))
    jmh(project(":boudicca.base:ingest-client"))
    jmh("com.fasterxml.jackson.core:jackson-core:2.18.1")
    jmh("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.1")
    jmh("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.1")
    jmh("com.fasterxml.jackson.core:jackson-databind:2.18.1")
}