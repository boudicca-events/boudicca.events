/**
 * plugin for applying the correct kotlin version
 */

plugins {
    id("boudicca-base")
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

detekt {
    config.setFrom("${project.rootDir}/default-detekt-config.yml")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    implementation(versionCatalog.findLibrary("slf4j").get())
    implementation(versionCatalog.findLibrary("kotlin-logging").get())
    testImplementation(platform(versionCatalog.findLibrary("junit-jupiter-bom").get()))
    testImplementation(versionCatalog.findLibrary("junit-jupiter").get())
    testRuntimeOnly(versionCatalog.findLibrary("junit-platform-launcher").get())
    testImplementation(versionCatalog.findLibrary("assertk").get())
    testImplementation(versionCatalog.findLibrary("mockk").get())
}

tasks.withType<Test> {
    useJUnitPlatform()
}
