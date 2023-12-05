plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("com.google.api-client:google-api-client:2.2.0") {
        exclude("commons-logging", "commons-logging")
    }
    implementation("com.google.auth:google-auth-library-oauth2-http:1.20.0") {
        exclude("commons-logging", "commons-logging")
    }
    implementation("com.google.apis:google-api-services-sheets:v4-rev20230815-2.0.0") {
        exclude("commons-logging", "commons-logging")
    }

    implementation(project(":boudicca.base:semantic-conventions"))
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

task<Exec>("imageBuild") {
    inputs.file("src/main/docker/Dockerfile")
    inputs.files(tasks.named("bootJar"))
    dependsOn(tasks.named("assemble"))
    commandLine("docker", "build", "-t", "localhost/boudicca-enricher", "-f", "src/main/docker/Dockerfile", ".")
}