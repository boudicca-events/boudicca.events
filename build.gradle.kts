version = "0.6.0-SNAPSHOT"
ext["jvmVersion"] = 21

val containerEngine by extra { "docker" } // or "podman"
//val containerEngine by extra { "podman" }