plugins {
    id("boudicca-kotlin")
}

group = "events.boudicca"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":boudicca.base:eventcollector-client"))
    implementation("org.jsoup:jsoup:1.18.2")
    implementation("com.rometools:rome:2.1.0")
    implementation("com.beust:klaxon:5.6")
}

task<Exec>("imageBuild") {
    inputs.file("src/main/docker/Dockerfile")
    inputs.files(tasks.named("jar"))
    dependsOn(tasks.named("assemble"))
    commandLine("docker", "build", "-t", "localhost/boudicca-events-eventcollectors", "-f", "src/main/docker/Dockerfile", ".")
}

tasks.withType<Jar> {
    archiveFileName.set("boudicca-eventcollectors.jar")

    manifest {
        attributes["Main-Class"] = "events.boudicca.eventcollector.BoudiccaEventCollectorsKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    inputs.files(configurations.runtimeClasspath)
    from(configurations.runtimeClasspath.get().files.map { if (it.isDirectory()) it else zipTree(it) })
}