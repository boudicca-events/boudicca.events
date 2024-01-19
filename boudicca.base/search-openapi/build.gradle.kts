plugins {
    id("org.openapi.generator") version "7.2.0"
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
//    openapi(project(mapOf("path" to ":search", "configuration" to "openapi")))
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

tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
    inputs.files(openapi)
    inputSpec.set(openapi.files.first().path)
    generatorName.set("java")
    library.set("native")
    additionalProperties.put("supportUrlQuery", "false")
    invokerPackage.set("base.boudicca.search.openapi")
    apiPackage.set("base.boudicca.search.openapi.api")
    modelPackage.set("base.boudicca.search.openapi.model")
}

sourceSets {
    main {
        java {
            srcDir(file("${layout.buildDirectory.get()}/generate-resources/main/src/main/java"))
        }
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>())
}