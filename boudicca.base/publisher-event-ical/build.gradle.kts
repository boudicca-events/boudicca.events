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
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation("org.mnode.ical4j:ical4j:3.2.14") {
        exclude("org.codehaus.groovy", "groovy")
        exclude("org.codehaus.groovy", "groovy-dateutil")
        exclude("commons-logging", "commons-logging")
    }
    implementation(project(":boudicca.base:search-client"))
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
    commandLine("docker", "build", "-t", "localhost/boudicca-ical", "-f", "src/main/docker/Dockerfile", ".")
}