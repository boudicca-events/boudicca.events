import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.openapi.generator") version "7.1.0"
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(rootProject.ext["jvmVersion"] as Int))
    }
}

val openapi by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val jacksonVersion = "2.16.0"
val jakartaAnnotationVersion = "1.3.5"

dependencies {
    openapi(files("src/main/resources/api-docs.json"))

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationVersion")
    api(project(":boudicca.base:semantic-conventions"))
}

tasks.withType<GenerateTask> {
    inputs.files(openapi)
    inputSpec.set(openapi.files.first().path)
}

tasks.register<GenerateTask>("generateJavaClient") {
    inputs.files(openapi)
    inputSpec.set(openapi.files.first().path)
    outputDir.set("${layout.buildDirectory}/generated/java")
    generatorName.set("java")
    library.set("native")
    additionalProperties.put("supportUrlQuery", "false")
    invokerPackage.set("events.boudicca.openapi")
    apiPackage.set("events.boudicca.openapi.api")
    modelPackage.set("events.boudicca.openapi.model")
}

tasks.register<GenerateTask>("generateTypescriptClient") {
    inputs.files(openapi)
    inputSpec.set(openapi.files.first().path)
    outputDir.set("${layout.buildDirectory}/generated/typescript")
    generatorName.set("typescript-axios")
    configOptions.putAll(
        mapOf(
            "npmName" to "@boudicca/search-api-client",
            "npmVersion" to "${project.version}",
        )
    )
}

sourceSets {
    main {
        java {
            srcDir(file("${layout.buildDirectory.get()}/generated/java/src/main/java"))
        }
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.withType<GenerateTask>())
}