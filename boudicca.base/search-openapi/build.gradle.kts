plugins {
    id("boudicca-openapi-generate-client")
}

publishing {
    publications {
        create<MavenPublication>("search-client") {
            from(components["java"])
        }
    }
}