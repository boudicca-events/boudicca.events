plugins {
    id("boudicca-springboot-app")
}

group = "events.boudicca"

dependencies {
    implementation(project(":boudicca.base:publisher-event-html"))
}

docker {
    imageName = "boudicca-events-publisher-event-html"
}
