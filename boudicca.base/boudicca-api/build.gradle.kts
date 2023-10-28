plugins {
    id("io.cloudflight.autoconfigure.swagger-api-configure")
}

description = "Boudicca Base API"
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