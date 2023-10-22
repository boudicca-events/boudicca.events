plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.javaParameters = true
}

//TODO rename?
