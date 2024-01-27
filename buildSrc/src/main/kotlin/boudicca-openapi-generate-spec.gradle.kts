import io.swagger.v3.plugins.gradle.tasks.ResolveTask

plugins {
    kotlin("kapt")
    id("boudicca-kotlin")
    id("boudicca-publish")
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

val openapiSpec: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("openapiSpec"))
    }
}

val versionCatalog = versionCatalogs.named("libs")
dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(versionCatalog.findBundle("openapi-generate-spec").get())
}

val openApiDefaultsTemplate = project.rootDir.resolve("openapi_defaults/openapi.yaml")
val openApiDefaultsRendered = project.layout.buildDirectory.file("tmp/openapi/openapi.yaml").get().asFile
openApiDefaultsRendered.parentFile.mkdirs()
openApiDefaultsRendered.writeText("dummy")

val generateOpenApiSpecTask = tasks.named<ResolveTask>("resolve") {
    outputFileName = project.name
    outputFormat = ResolveTask.Format.JSON
    prettyPrint = true
    classpath = sourceSets.main.get().runtimeClasspath
    resourcePackages = setOf("base.boudicca.api")
    outputDir = project.layout.buildDirectory.dir("generated/openapi").get().asFile
    openApiFile = openApiDefaultsRendered
    doFirst {
        updateOpenApiFile()
    }
}

artifacts {
    add("openapiSpec", project.layout.buildDirectory.file("generated/openapi/${project.name}.json")) {
        builtBy(generateOpenApiSpecTask)
    }
}

val generateSpecExtension = GenerateSpecExtension(null, null)
project.extensions.create<GenerateSpecExtension>("generateSpec")

fun updateOpenApiFile() {
    val text = openApiDefaultsTemplate.readText()
        .replace("%TITLE%", generateSpecExtension.title ?: "")
        .replace("%DESCRIPTION%", generateSpecExtension.description ?: "")
        .replace("%VERSION%", project.version.toString())
    openApiDefaultsRendered.writeText(text)
}

open class GenerateSpecExtension(
    var title: String? = null,
    var description: String? = null,
)