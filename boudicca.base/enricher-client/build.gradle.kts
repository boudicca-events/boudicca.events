plugins {
    id("boudicca-kotlin")
    `maven-publish`
}

dependencies {
    api(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:enricher-openapi"))
}

publishing {
    publications {
        create<MavenPublication>("enricher-client") {
            from(components["java"])
        }
    }
}