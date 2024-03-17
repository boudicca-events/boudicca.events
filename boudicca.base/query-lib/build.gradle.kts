plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:semantic-conventions"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}