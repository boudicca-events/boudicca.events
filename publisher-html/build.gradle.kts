import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("org.springframework.boot") version "3.0.6"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm")
	kotlin("plugin.spring")
	id("com.netflix.nebula.jakartaee-migration") version "0.9.0"
}

group = "events.boudicca"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.github.jknack:handlebars:4.3.1")
	implementation("com.github.jknack:handlebars-springmvc:4.3.1")
	implementation(project(":search-openapi"))
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jakartaeeMigration {
	migrate()
}

tasks.withType<Jar> {
	archiveFileName.set("boudicca-html.jar")
}

task<Exec>("imageBuild") {
	dependsOn(tasks.withType<BootJar>())
	commandLine("docker", "build", "-t", "boudicca-html", "-f", "src/main/docker/Dockerfile", ".")
}