plugins {
    kotlin("jvm")
    `maven-publish`
}



kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
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