plugins {
    id("boudicca-kotlin")
    id("boudicca-docker")
}

group = "events.boudicca"

dependencies {
    implementation(project(":boudicca.base:eventcollector-client"))
    implementation(libs.jsoup)
    implementation(libs.rometools)
    implementation(libs.klaxon)
}

docker {
    imageName = "events-eventcollectors"
    jarCreationTaskName = "jar"
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
