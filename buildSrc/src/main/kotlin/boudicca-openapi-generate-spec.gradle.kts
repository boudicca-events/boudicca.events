import io.swagger.v3.plugins.gradle.tasks.ResolveTask

/**
 * this plugin creates an openapi.json-spec by scanning all interfaces in the package "base.boudicca.api.*"
 * also publishes the interface classes as a jar and the generated openapi.json with classifier "openapi"
 * also exposes the generated openapi spec file in the "openapiSpec" configuration to be consumed by other projects.
 * this configuration has the attribute LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE set to "openapiSpec"
 *
 * this plugin also registers the extension property "generateSpec" where you can set the title and description which will be merged into the openapi.json file
 *
 * please note that interfaces have to have the @OpenAPIDefinition annotation but is NOT allowed to set the info property in that annotation.
 * if you need additional info properties you have to extend GenerateSpecExtension and add it in the template "openapi_defaults/openapi.yaml" with a placeholder which should be replaced in the "updateOpenApiFile()" method here
 */

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
    implementation(project(":boudicca.base:common-model"))
    implementation(versionCatalog.findBundle("openapi-generate-spec").get())
}

val generateSpecExtension = project.extensions.create<GenerateSpecExtension>("generateSpec")

val createTemplateTask = tasks.register<CreateTemplateTask>("myTestTask") {
    templateFile.set(project.rootDir.resolve("openapi_defaults/openapi.yaml"))
    renderedOutput.set(project.layout.buildDirectory.file("tmp/openapi/openapi.yaml").get().asFile)
    openApiVersion.set(project.version.toString())
    openApiTitle = generateSpecExtension.title
    openApiDescription = generateSpecExtension.description
}

val generateOpenApiSpecTask = tasks.named<ResolveTask>("resolve") {
    inputs.files(createTemplateTask)
    outputFileName = project.name
    outputFormat = ResolveTask.Format.JSON
    prettyPrint = true
    classpath = sourceSets.main.get().runtimeClasspath
    resourcePackages = setOf("base.boudicca.api")
    outputDir = project.layout.buildDirectory.dir("generated/openapi").get().asFile
    openApiFile = createTemplateTask.get().renderedOutput.get().asFile
}

val openapiSpecFile = project.layout.buildDirectory.file("generated/openapi/${project.name}.json")
artifacts {
    add("openapiSpec", openapiSpecFile) {
        builtBy(generateOpenApiSpecTask)
    }
}

publishing {
    publications {
        named<MavenPublication>(project.name) {
            artifact(mapOf("source" to openapiSpecFile, "classifier" to "openapi")) {
                builtBy(generateOpenApiSpecTask)
            }
        }
    }
}

interface GenerateSpecExtension {
    val title: Property<String>
    val description: Property<String>
}

abstract class CreateTemplateTask : DefaultTask() {

    @get:InputFile
    abstract val templateFile: RegularFileProperty

    @get:OutputFile
    abstract val renderedOutput: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val openApiVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val openApiTitle: Property<String>

    @get:Input
    @get:Optional
    abstract val openApiDescription: Property<String>

    @TaskAction
    fun action() {
        renderedOutput.get().asFile.parentFile.mkdirs()
        val text = templateFile.get().asFile.readText()
            .replace("%TITLE%", openApiTitle.getOrElse(""))
            .replace("%DESCRIPTION%", openApiDescription.getOrElse(""))
            .replace("%VERSION%", openApiVersion.getOrElse(""))
        renderedOutput.get().asFile.writeText(text)
    }
}
