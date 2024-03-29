/**
 * plugin which will publish everything from the "java" component
 * this plugin will also sign artifacts when the property "doSign=true" is set
 *
 * you can then either publishToMavenLocal, or "publish", which will publish into the directory "<rootDir>/build/release_repo"
 * this is done because maven central currently has no gradle plugin which can publish, so publishing into the repo and then collecting all the files is the workaround -.-
 */

plugins {
    id("boudicca-java-library")
    `maven-publish`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven{
            name = "releaseRepo"
            url = uri(rootProject.layout.buildDirectory.dir("release_repo"))
        }
    }
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
            artifactId = project.name
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
    sign(publishing.publications[project.name])
}

tasks.withType<Sign>().configureEach {
    onlyIf("needs signing") {
        project.findProperty("doSign") == "true" || System.getProperty("doSign") == "true"
    }
    doFirst { println("signing!") }
}
