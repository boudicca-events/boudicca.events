import gradle.kotlin.dsl.accessors._75d5f13ca443f8b16e6e50ca4ba45c16.sourceSets
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType

/**
 * plugin for applying the correct kotlin version
 */

plugins {
    id("boudicca-base")
    kotlin("multiplatform")
    // disable until https://github.com/boudicca-events/boudicca.events/issues/663 is discussed
    // id("io.gitlab.arturbosch.detekt")
}

val versionCatalog = versionCatalogs.named("libs")
kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    jvm {
        compilerOptions {
            javaParameters = true
        }
    }
    js(IR) {
        nodejs() // For Node.js environment
        binaries.library() // For creating a library
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(versionCatalog.findLibrary("kotlin-logging").get())
            // TODO: version catalog
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
        }

        jvmMain.dependencies {
            implementation(versionCatalog.findLibrary("slf4j").get())
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
