plugins {
    id("boudicca-springboot-app")
}

group = "events.boudicca"

dependencies {
    implementation(project(":boudicca.base:eventcollector-client"))
    implementation(libs.jsoup)
    implementation(libs.rometools)
    implementation(libs.klaxon)
}

springBoot {
    mainClass.set("events.boudicca.eventcollector.BoudiccaEventCollectorsKt")
}

docker {
    imageName = "events-eventcollectors"
}
