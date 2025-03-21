/**
 * plugin which will publish everything from the "java" component
 * this plugin will also sign artifacts when the property "doSign=true" is set
 *
 * you can then either publishToMavenLocal, or "publish", which will publish into the directory "<rootDir>/build/release_repo"
 * this is done because maven central currently has no gradle plugin which can publish, so publishing into the repo and then collecting all the files is the workaround -.-
 */

plugins {
    id("boudicca-java-library")
    id("boudicca-publish-configure")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
            artifactId = project.name
        }
    }
}
