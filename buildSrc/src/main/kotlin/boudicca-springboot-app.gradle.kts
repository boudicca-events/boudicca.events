plugins {
    id("boudicca-kotlin")

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    api(versionCatalog.findBundle("springboot-app").get())

    developmentOnly(versionCatalog.findLibrary("spring-boot-devtools").get())
    testImplementation(versionCatalog.findLibrary("spring-boot-starter-test").get())
}

task<Exec>("imageBuild") {
    inputs.file("src/main/docker/Dockerfile")
    inputs.files(tasks.named("bootJar")) //TODO extract to own plugin
    dependsOn(tasks.named("assemble"))
    commandLine("docker", "build", "-t", "localhost/boudicca-${project.name}", "-f", "src/main/docker/Dockerfile", ".")
}

