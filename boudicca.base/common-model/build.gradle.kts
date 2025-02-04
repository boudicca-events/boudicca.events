plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
