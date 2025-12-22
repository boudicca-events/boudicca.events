plugins {
    id("boudicca-openapi-generate-client")
}

dependencies {
    openapi(project(":boudicca.base:enricher-api"))
}
