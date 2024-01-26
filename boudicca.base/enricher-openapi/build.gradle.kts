plugins {
    id("boudicca-openapi-generate-client")
}

publishing {
    publications {
        create<MavenPublication>("enricher-client") {
            from(components["java"])
        }
    }
}