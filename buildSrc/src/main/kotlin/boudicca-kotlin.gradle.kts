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

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    testImplementation(platform(versionCatalog.findLibrary("junit-jupiter-bom").get()))
    testImplementation(versionCatalog.findLibrary("junit-jupiter").get())
    testRuntimeOnly(versionCatalog.findLibrary("junit-platform-launcher").get())
    testImplementation(versionCatalog.findLibrary("assertk").get())
    testImplementation(versionCatalog.findLibrary("mockk").get())
}

tasks.withType<Test> {
    useJUnitPlatform()
}
