plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")

    kotlin("plugin.spring")
}

dependencies {
    implementation(libs.otel.sdk)
    implementation(libs.otel.exporter)
    implementation(libs.otel.semconv)
    implementation(libs.otel.micrometer)
    implementation(libs.otel.logback)
    implementation(libs.micrometer.core)
    implementation(libs.spring.boot.starter)
}
