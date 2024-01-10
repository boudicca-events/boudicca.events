plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("kapt") version "1.9.22"
    id("io.cloudflight.autoconfigure.swagger-api-configure") version "1.1.0" apply false
    kotlin("plugin.allopen") version "1.9.21" apply false
    kotlin("plugin.spring") version "1.9.21" apply false
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

ext["jvmVersion"] = 21

subprojects.forEach {
    if (!it.name.endsWith("-ui") && !it.name.endsWith("-html")) {
        dependencies {
            implementation(platform(libs.cloudflight.platform.spring.bom))
            annotationProcessor(platform(libs.cloudflight.platform.spring.bom))
            testImplementation(platform(libs.cloudflight.platform.spring.test.bom))
            kapt(platform(libs.cloudflight.platform.spring.bom))
        }
    }
}
