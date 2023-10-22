plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api(project(":boudicca.base:semantic-conventions"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":boudicca.base:enricher-openapi"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.javaParameters = true
}
