/**
 * plugin for applying the correct kotlin version
 */

plugins {
    id("boudicca-base")
    kotlin("jvm")
}

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}
