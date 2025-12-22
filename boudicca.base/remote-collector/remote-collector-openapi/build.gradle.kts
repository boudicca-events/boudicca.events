plugins {
    id("boudicca-openapi-generate-client")
}

dependencies {
    openapi(project(":boudicca.base:remote-collector:remote-collector-api"))
}
