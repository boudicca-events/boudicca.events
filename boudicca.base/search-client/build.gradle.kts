plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":boudicca.base:semantic-conventions"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:search-openapi"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        javaParameters = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}