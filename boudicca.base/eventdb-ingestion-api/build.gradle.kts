plugins {
    kotlin("jvm")
}

description = "Boudicca EventDB Ingestion API"
version = "0.0.1"

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation("io.swagger:swagger-annotations:1.6.12")
    implementation("org.springframework:spring-web:6.1.1")
    implementation("org.springframework:spring-context:6.1.1")
}