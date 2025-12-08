plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.bundles.boudicca.plugins)
}

kotlin {
    jvmToolchain(21) // be careful to keep this in sync with the global build.gradle.kts
}
