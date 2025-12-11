/**
 * plugin for applying the correct kotlin version
 */

plugins {
    id("boudicca-base")
    kotlin("jvm")
    id("dev.detekt")
    id("boudicca-jacoco")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("${project.rootDir}/detekt-config.yml")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    implementation(versionCatalog.findLibrary("slf4j").get())
    implementation(versionCatalog.findLibrary("kotlin-logging").get())
    implementation(versionCatalog.findLibrary("otel-api").get())
    testImplementation(platform(versionCatalog.findLibrary("junit-jupiter-bom").get()))
    testImplementation(versionCatalog.findLibrary("junit-jupiter").get())
    testRuntimeOnly(versionCatalog.findLibrary("junit-platform-launcher").get())
    testImplementation(versionCatalog.findLibrary("assertk").get())
    testImplementation(versionCatalog.findLibrary("mockk").get())
}
