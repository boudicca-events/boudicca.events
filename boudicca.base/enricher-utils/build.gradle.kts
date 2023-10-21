plugins {
    kotlin("jvm")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20231013")
    implementation(project(":boudicca.base:enricher-api"))
    implementation(project(":boudicca.base:publisher-api"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.javaParameters = true
}
