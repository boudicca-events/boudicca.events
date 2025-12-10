/**
 * plugin for applying the correct java version
 */
plugins {
    id("boudicca-base")
    `java-library`
    id("boudicca-jacoco")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(rootProject.ext["jvmVersion"] as Int))
    }
}
