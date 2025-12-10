plugins {
    jacoco
}

jacoco {
    toolVersion = versionCatalogs.named("libs").findVersion("jacoco").get().requiredVersion
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Register this project for aggregation in root project
rootProject.dependencies {
    add("jacocoAggregation", project)
}
