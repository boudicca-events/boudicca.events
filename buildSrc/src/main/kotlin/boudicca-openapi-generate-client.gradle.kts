import org.gradle.kotlin.dsl.*
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

/**
 * this plugin generates a java client, users of this plugin have to add a project dependency with the configuration "openapi" on a project with the "boudicca-openapi-generate-spec" plugin applied
 * this plugin also publishes the client which was generated
 */

plugins {
    id("boudicca-java-library")
    id("org.openapi.generator")
    id("boudicca-publish")
}

val openapi by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class.java, "openapiSpec"))
    }
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    api(project(":boudicca.base:semantic-conventions"))

    implementation(versionCatalog.findBundle("openapi-generate-client").get())
}

val openApiPackageName = project.name.substring(0, project.name.lastIndexOf("-"))
tasks.register<GenerateTask>("generateJavaClient") {
    doFirst {
        delete(layout.buildDirectory.dir("generated/java").get())
    }
    inputs.files(openapi)
    inputSpec.set(getInputSpecProvider())
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
    doFirst {
        delete(layout.buildDirectory.dir("generated/typescript").get())
    }
    inputs.files(openapi)
    inputSpec.set(getInputSpecProvider())
    outputDir.set(layout.buildDirectory.dir("generated/typescript").get().toString())
    generatorName.set("typescript-axios")
    templateDir.set(project.rootDir.resolve("typescript_generator_overrides").path)
    configOptions.putAll(
        mapOf(
            "npmName" to "@boudicca/${openApiPackageName}",
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

tasks.named("sourcesJar") {
    inputs.files(tasks.named("generateJavaClient"))
}

fun getInputSpecProvider(): Provider<String> {
    return provider {
        openapi.files.firstOrNull()?.path
    }
}