plugins {
    id("boudicca-springboot-app-nodevtools") // devtools fuck up a lot in here... one example is log capturing and the whole Collections api...
}

group = "events.boudicca"

dependencies {
    implementation(project(":boudicca.base:eventcollector-client"))
    implementation(libs.jsoup)
    implementation(libs.rometools)
    implementation(libs.klaxon)
    implementation(libs.flexmark.all)
    implementation(libs.flexmark.html2md)
}

springBoot {
    mainClass.set("events.boudicca.eventcollector.BoudiccaEventCollectorsKt")
}

docker {
    imageName = "events-eventcollectors"
}
