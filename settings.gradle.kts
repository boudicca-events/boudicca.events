include("api-model")
include("ingestion-api")
include("crawler-jku")
include("crawler-technologieplauscherl")
include("crawler-posthof")
include("query-api")
include("publisher-ical")
include("publisher-html")
include("core")

pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}
rootProject.name = "boudicca"