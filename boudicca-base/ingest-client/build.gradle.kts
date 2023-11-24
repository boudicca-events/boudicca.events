plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        javaParameters = true
    }
}

dependencies {
    api(project(":boudicca-base:semantic-conventions"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":boudicca-base:semantic-conventions"))
    implementation(project(":boudicca-base:eventdb-openapi"))
}
