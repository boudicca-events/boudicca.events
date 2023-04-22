include("core")
include("crawler-api")
include("crawler-demo")
include("crawler-jku")
include("crawler-technologieplauscherl")
include("publisher-api")
include("publisher-ical")
include("publisher-html")

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