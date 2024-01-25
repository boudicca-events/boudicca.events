plugins {
    id("boudicca-base")
    `maven-publish`
}

// TODO: rename this package to common-model or something


publishing {
    publications {
        create<MavenPublication>("semantic-conventions") {
            from(components["java"])
        }
    }
}