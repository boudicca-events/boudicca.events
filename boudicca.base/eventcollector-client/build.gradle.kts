plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    `maven-publish`
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.apache.velocity:velocity-engine-core:2.3")
    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    api("org.slf4j:slf4j-api:2.0.12")
    implementation(project(":boudicca.base:ingest-client"))
    implementation(project(":boudicca.base:enricher-client"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("eventcollector-client") {
            from(components["java"])
        }
    }
}