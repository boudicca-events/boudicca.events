plugins {
    id("boudicca-springboot-rest-app")
}

dependencies {
    implementation(libs.google.api.client) {
        exclude("commons-logging", "commons-logging")
    }
    implementation(libs.google.auth.library.oauth2.http) {
        exclude("commons-logging", "commons-logging")
    }
    implementation(libs.google.api.services.sheets) {
        exclude("commons-logging", "commons-logging")
    }

    implementation(project(":boudicca.base:enricher-api"))
    implementation(project(":boudicca.base:common-model"))
    implementation(project(":boudicca.base:fetcher-lib"))
}
