plugins {
    id("boudicca-springboot-rest-app")
}

group = "events.boudicca"

dependencies {
    implementation(project(":boudicca.base:enricher"))
    implementation(project(":boudicca.base:fetcher-lib"))

    implementation(libs.google.api.client) {
        exclude("commons-logging", "commons-logging")
    }
    implementation(libs.google.auth.library.oauth2.http) {
        exclude("commons-logging", "commons-logging")
    }
    implementation(libs.google.api.services.sheets) {
        exclude("commons-logging", "commons-logging")
    }
}
