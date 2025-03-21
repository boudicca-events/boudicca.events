import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType

/**
 * plugin for setting up kotlin multiplatform builds
 */

plugins {
    id("dev.petuska.npm.publish")
    kotlin("multiplatform")
    id("boudicca-base")
    id("boudicca-publish-configure")
    // disable until https://github.com/boudicca-events/boudicca.events/issues/663 is discussed
//     id("io.gitlab.arturbosch.detekt")
}

val versionCatalog = versionCatalogs.named("libs")
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    jvm {
        mavenPublication { }
        compilerOptions {
            javaParameters = true
        }
    }
    js(IR) {
        nodejs()
        binaries.library() // For creating a library
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(versionCatalog.findLibrary("kotlin-logging").get())
        }

        jvmMain.dependencies {
            implementation(versionCatalog.findLibrary("kotlin-logging-jvm").get())
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmTest.dependencies {
            implementation(project.dependencies.platform(versionCatalog.findLibrary("junit-jupiter-bom").get()))
            implementation(versionCatalog.findLibrary("junit-jupiter").get())
            implementation(versionCatalog.findLibrary("assertk").get())
            implementation(versionCatalog.findLibrary("mockk").get())
            runtimeOnly(versionCatalog.findLibrary("junit-platform-launcher").get())
        }
    }
}

//detekt {
//    config.setFrom("${project.rootDir}/default-detekt-config.yml")
//}

tasks.withType<Test> {
    useJUnitPlatform()
}

npmPublish {
    registries {
        // For registries expecting an authentiation token, use authToken
        register("npmjs") {
            uri.set("https://registry.npmjs.org")
            authToken.set("obfuscated")
        }
    }
}
