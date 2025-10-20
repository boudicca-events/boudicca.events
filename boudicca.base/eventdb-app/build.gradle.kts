plugins {
    id("boudicca-springboot-rest-app")
}


dependencies {
    implementation(project(":boudicca.base:eventdb"))
    testImplementation(libs.spring.security.test)
}

docker {
    imageName = "eventdb"
}
