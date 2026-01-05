plugins {
    id("boudicca-springboot-lib")
}

dependencies {
    // implementation(libs.spring.boot.starter.web)
    // implementation("org.springframework.boot:spring-boot-autoconfigure:4.0.1")

    implementation("org.springframework.ai:spring-ai-autoconfigure-model-chat-client:2.0.0-M1")
    implementation("org.springframework.ai:spring-ai-starter-model-mistral-ai:2.0.0-M1")
}
