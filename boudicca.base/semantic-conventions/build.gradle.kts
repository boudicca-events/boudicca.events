plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":boudicca.base:boudicca-api"))
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        javaParameters = true
    }
}

//TODO rename?
