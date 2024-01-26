import org.gradle.kotlin.dsl.*
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("boudicca-java-library")
    id("org.openapi.generator")
    `maven-publish`
}

val openapi by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    openapi(files("src/main/resources/api-docs.json"))
    api(project(":boudicca.base:semantic-conventions"))

    implementation(versionCatalog.findBundle("openapi-generate-client").get())
}

val openApiPackageName = project.name.substring(0, project.name.lastIndexOf("-"))
tasks.register<GenerateTask>("generateJavaClient") {
    inputs.files(openapi)
    inputSpec.set(openapi.files.first().path)
    outputDir.set(layout.buildDirectory.dir("generated/java").get().toString())
    generatorName.set("java")
    library.set("native")
    additionalProperties.put("supportUrlQuery", "false")
    generateApiTests.set(false)
    generateModelTests.set(false)
    invokerPackage.set("base.boudicca.openapi")
    apiPackage.set("base.boudicca.${openApiPackageName}.openapi.api")
    modelPackage.set("base.boudicca.${openApiPackageName}.openapi.model")
}

tasks.register<GenerateTask>("generateTypescriptClient") {
    inputs.files(openapi)
    inputSpec.set(openapi.files.first().path)
    outputDir.set(layout.buildDirectory.dir("generated/typescript").get().toString())
    generatorName.set("typescript-axios")
    templateDir.set(project.rootDir.resolve("typescript_generator_overrides").path)
    configOptions.putAll(
        mapOf(
            "npmName" to "@boudicca/search-api-client",
            "npmVersion" to "${project.version}",
            "supportsES6" to "true",
        )
    )
}

sourceSets {
    main {
        java {
            srcDir(file(layout.buildDirectory.dir("generated/java/src/main/java").get().toString()))
        }
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named<GenerateTask>("generateJavaClient"))
}
