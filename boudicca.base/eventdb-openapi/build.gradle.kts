plugins {
    id("boudicca-openapi-generate-client")
}

publishing {
    publications {
        create<MavenPublication>("eventdb-client") {
            from(components["java"])
        }
    }
}