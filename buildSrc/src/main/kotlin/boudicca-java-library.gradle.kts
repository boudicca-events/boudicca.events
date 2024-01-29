import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * plugin for applying the correct java version
 */
plugins {
    id("boudicca-base")
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(rootProject.ext["jvmVersion"] as Int))
    }
}