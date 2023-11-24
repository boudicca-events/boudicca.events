plugins {
    kotlin("jvm")
    id("io.cloudflight.autoconfigure.swagger-api-configure") version "1.1.0"
}

description = "Boudicca EventDB Publisher API"
version = "0.0.1"

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
    implementation(project(":boudicca-base:semantic-conventions"))
    implementation("io.swagger:swagger-annotations:1.6.12")
    implementation("org.springframework:spring-web:6.0.13")
    implementation("org.springframework:spring-context:6.0.13")
}