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
