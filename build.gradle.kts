version = file("version.txt").readText()
ext["jvmVersion"] = 21

val containerEngine by extra { "docker" } // or "podman"
//val containerEngine by extra { "podman" }