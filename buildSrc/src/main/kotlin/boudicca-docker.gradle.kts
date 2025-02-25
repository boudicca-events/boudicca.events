import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.task

interface DockerPluginExtension {
    val imageName: Property<String>
    val jarCreationTaskName: Property<String>
}

val extension = project.extensions.create<DockerPluginExtension>("docker")
extension.imageName.convention(project.name)
extension.jarCreationTaskName.convention("bootJar")

tasks.register<Exec>("imageBuild") {
    inputs.file("src/main/docker/Dockerfile")
    inputs.files(tasks.named(extension.jarCreationTaskName.get()))
    dependsOn(tasks.named("assemble"))
    commandLine("docker", "build", "-t", "localhost/${extension.imageName.get()}", "-f", "src/main/docker/Dockerfile", ".")
}
