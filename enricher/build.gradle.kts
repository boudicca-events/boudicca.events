import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm")
    kotlin("plugin.spring")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation("com.google.api-client:google-api-client:2.0.0") {
        exclude("commons-logging","commons-logging")
    }
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0") {
        exclude("commons-logging","commons-logging")
    }
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0") {
        exclude("commons-logging","commons-logging")
    }

    implementation(project(":semantic-conventions"))
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

task<Exec>("imageBuild") {
    inputs.file("src/main/docker/Dockerfile")
    inputs.files(tasks.named("bootJar"))
    dependsOn(tasks.named("assemble"))
    commandLine("docker", "build", "-t", "boudicca-enricher", "-f", "src/main/docker/Dockerfile", ".")
}