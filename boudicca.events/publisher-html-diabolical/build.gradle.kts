import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
	id("boudicca-kotlin")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	kotlin("plugin.spring")
}

group = "events.boudicca"

dependencies {
	implementation(project(":boudicca.base:publisher-event-html"))
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named<BootJar>("bootJar") {
	mainClass.set("base.boudicca.publisher.event.html.PublisherHtmlApplicationKt")
}

tasks.named<BootRun>("bootRun") {
	mainClass.set("base.boudicca.publisher.event.html.PublisherHtmlApplicationKt")
}

task<Exec>("imageBuild") {
	inputs.file("src/main/docker/Dockerfile")
	inputs.files(tasks.named("bootJar"))
	dependsOn(tasks.named("assemble"))
	commandLine("docker", "build", "-t", "localhost/boudicca-events-boudicca-html-diabolical", "-f", "src/main/docker/Dockerfile", ".")
}