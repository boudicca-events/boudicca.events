plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:eventdb-openapi"))
}
