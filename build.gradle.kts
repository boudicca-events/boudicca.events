plugins {
    base
    id("jacoco-report-aggregation")
    alias(libs.plugins.sonarqube)
}

version = file("version.txt").readText()

dependencyLocking {
    lockAllConfigurations()
}

tasks.register("writeLocks") {
    description = "Regenerates the dependency lock file for this project (run with --write-locks)"
    group = "build setup"
    dependsOn("dependencies")
}
ext["jvmVersion"] = 21 // be careful to keep this in sync with the buildSrc/build.gradle.kts

repositories {
    mavenCentral()
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testSuiteName = "test"
            reportTask {
                classDirectories.setFrom(
                    files(
                        classDirectories.files.map {
                            fileTree(it) {
                                exclude("**/openapi/**")
                            }
                        },
                    ),
                )
            }
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "boudicca-events_boudicca.events")
        property("sonar.organization", "boudicca-events")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml",
        )
    }
}
