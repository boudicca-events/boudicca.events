plugins {
    id("org.openapi.generator") version "7.0.1"
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

val jacksonVersion = "2.15.2"
val jakartaAnnotationVersion = "2.0.0"

dependencies {
//    openapi(project(mapOf("path" to ":search", "configuration" to "openapi")))
    openapi(files("src/main/resources/openapi.yml"))

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationVersion")
    api(project(":semantic-conventions"))
}

tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
    inputs.files(openapi)
    inputSpec.set(openapi.files.first().path)
    generatorName.set("java")
    library.set("native")
    additionalProperties.put("supportUrlQuery", "false")
    invokerPackage.set("events.boudicca.search.openapi")
    apiPackage.set("events.boudicca.search.openapi.api")
    modelPackage.set("events.boudicca.search.openapi.model")
}

sourceSets {
    main {
        java {
            srcDir(file("${layout.buildDirectory.get()}/generate-resources/main/src/main/java"))
        }
    }
}

tasks.named("compileJava"){
    dependsOn(tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>())
}