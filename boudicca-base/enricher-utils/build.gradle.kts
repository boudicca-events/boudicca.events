plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        javaParameters = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20231013")
    implementation(project(":boudicca-base:enricher-client"))
    implementation(project(":boudicca-base:publisher-client"))
}
