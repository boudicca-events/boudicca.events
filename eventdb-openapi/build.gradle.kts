plugins {
    id("org.openapi.generator") version "6.5.0"
    `java-library`
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val openapi by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val jackson_version = "2.15.2"
val jakarta_annotation_version = "1.3.5"

dependencies {
//    openapi(project(mapOf("configuration" to "openapi", "path" to ":eventdb")))
    openapi(files("src/main/resources/openapi.yml"))

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.fasterxml.jackson.core:jackson-core:$jackson_version")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jackson_version")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakarta_annotation_version")
    api(project(":semantic-conventions"))
}

tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
    inputs.files(openapi)
    inputSpec.set(openapi.files.first().path)
    generatorName.set("java")
    library.set("native")
    additionalProperties.put("supportUrlQuery", "false")
    invokerPackage.set("events.boudicca.openapi")
    apiPackage.set("events.boudicca.openapi.api")
    modelPackage.set("events.boudicca.openapi.model")
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generate-resources/main/src/main/java"))
        }
    }
}

tasks.named("compileJava"){
    dependsOn(tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>())
}