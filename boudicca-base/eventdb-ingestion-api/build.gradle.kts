plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("io.cloudflight.autoconfigure.swagger-api-configure") version "1.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.cloudflight.platform.spring.bom))
    implementation("io.swagger:swagger-annotations")
    implementation("org.springframework:spring-web")
    implementation(project(":boudicca-base:semantic-conventions"))
}
