task<Exec>("imageBuild") {
    commandLine("docker", "build", "-t", "boudicca-html", ".")
}