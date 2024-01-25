import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("boudicca-base")
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(rootProject.ext["jvmVersion"] as Int))
    }
}