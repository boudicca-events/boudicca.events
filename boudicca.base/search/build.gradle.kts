plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm")
    kotlin("plugin.spring")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        javaParameters = true
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation(project(":boudicca.base:boudicca-api"))
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:publisher-client"))
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

task<Exec>("imageBuild") {
    inputs.file("src/main/docker/Dockerfile")
    inputs.files(tasks.named("bootJar"))
    dependsOn(tasks.named("assemble"))
    commandLine("docker", "build", "-t", "localhost/boudicca-search", "-f", "src/main/docker/Dockerfile", ".")
}