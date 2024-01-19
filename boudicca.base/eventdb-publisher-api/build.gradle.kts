plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("io.cloudflight.autoconfigure.swagger-api-configure")
}

description = "Boudicca EventDB Publisher API"
group = "base.boudicca.eventdb.publisher"  // it's necessary to set group here manually otherwise swagger will mix the APIs up

kotlin {
    jvmToolchain(rootProject.ext["jvmVersion"] as Int)
    compilerOptions {
        javaParameters = true
    }
}



dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(platform(libs.cloudflight.platform.spring.bom))
    implementation("io.swagger:swagger-annotations")
    implementation("org.springframework:spring-web")
}