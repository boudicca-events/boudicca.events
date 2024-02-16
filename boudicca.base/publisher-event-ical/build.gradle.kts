plugins {
    id("boudicca-springboot-app")
}

dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation("org.mnode.ical4j:ical4j:3.2.15") {
        exclude("org.codehaus.groovy", "groovy")
        exclude("org.codehaus.groovy", "groovy-dateutil")
        exclude("commons-logging", "commons-logging")
    }
    implementation(project(":boudicca.base:search-client"))
}
