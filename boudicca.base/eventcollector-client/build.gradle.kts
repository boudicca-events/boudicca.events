plugins {
    id("boudicca-kotlin")
    id("boudicca-publish")
}

dependencies {
    api(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:publisher-client"))
    implementation(project(":boudicca.base:ingest-client"))
    implementation(project(":boudicca.base:enricher-client"))
    implementation(project(":boudicca.base:remote-collector:remote-collector-client"))
    implementation("org.apache.velocity:velocity-engine-core:2.3")
    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")
    implementation("ch.qos.logback:logback-classic:1.5.4")
    implementation("org.slf4j:slf4j-api:2.0.13")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}
