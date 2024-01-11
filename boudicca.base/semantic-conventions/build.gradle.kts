plugins {
    kotlin("jvm")
    `maven-publish`
}

// TODO: rename this package to common-model or something



kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}

publishing {
    publications {
        create<MavenPublication>("semantic-conventions") {
            from(components["java"])
        }
    }
}