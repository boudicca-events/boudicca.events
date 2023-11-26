plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.apache.velocity:velocity-engine-core:2.3")
    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    api("org.slf4j:slf4j-api:2.0.9")
    implementation(project(":boudicca.base:ingest-client"))
    implementation(project(":boudicca.base:enricher-client"))
}