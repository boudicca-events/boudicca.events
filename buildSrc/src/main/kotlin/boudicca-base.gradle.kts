/**
 * base plugin which will apply default repos, group, version, ...
 * does not apply java or kotlin
 */

group = "base.boudicca"
version = rootProject.version
description = "Boudicca Events"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencyLocking {
    lockAllConfigurations()
}

tasks.register("writeLocks") {
    description = "Regenerates the dependency lock file for this project (run with --write-locks)"
    group = "build setup"
    dependsOn("dependencies")
}
