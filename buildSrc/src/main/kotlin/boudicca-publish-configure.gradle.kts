plugins {
    `maven-publish`
    signing
}

publishing {
    repositories {
        maven {
            name = "releaseRepo"
            url = uri(rootProject.layout.buildDirectory.dir("release_repo"))
        }
    }
    publications {
        withType<MavenPublication> {
            groupId = "events.boudicca"

            pom {
                name = "Boudicca.Events ${project.name}"
                description = "TODO"
                url = "https://github.com/boudicca-events/boudicca.events"
                licenses {
                    license {
                        name = "GNU General Public License, Version 3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                    }
                }
                developers {
                    developer {
                        id = "Boudicca.Events Team"
                        name = "Boudicca.Events Team"
                        email = "team@boudicca.events"
                    }
                }
                scm {
                    connection = "git@github.com:boudicca-events/boudicca.events.git"
                    developerConnection = "git@github.com:boudicca-events/boudicca.events.git"
                    url = "https://github.com/boudicca-events/boudicca.events"
                }
            }
        }
    }
}

signing {
    publishing.publications.forEach { sign(it) }
}

tasks.withType<Sign>().configureEach {
    onlyIf("needs signing") {
        project.findProperty("doSign") == "true" || System.getProperty("doSign") == "true"
    }
    doFirst { println("signing!") }
}
