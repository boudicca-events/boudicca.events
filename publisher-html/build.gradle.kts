import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("org.springframework.boot") version "3.1.4"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm")
	kotlin("plugin.spring")
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	api("org.springframework.boot:spring-boot-starter-web")
	api("com.fasterxml.jackson.module:jackson-module-kotlin")
	api("org.jetbrains.kotlin:kotlin-reflect")
	api("com.github.jknack:handlebars:4.3.1")
	api("events.boudicca:search-client:0.2.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

task<Exec>("imageBuild") {
	inputs.file("src/main/docker/Dockerfile")
	dependsOn(tasks.named("assemble"))
	commandLine("docker", "build", "-t", "boudicca-html", "-f", "src/main/docker/Dockerfile", ".")
}