include("openapi")
include("eventcollector-api")
include("eventcollectors")
include("publisher-api")
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