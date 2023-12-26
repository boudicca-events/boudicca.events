plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("io.cloudflight.autoconfigure.swagger-api-configure")
    `maven-publish`
}

description = "Boudicca Search API"
version = "0.0.1"
group = "base.boudicca"

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(platform(libs.cloudflight.platform.spring.bom))
    implementation("io.swagger:swagger-annotations")
    implementation("org.springframework:spring-web")
}