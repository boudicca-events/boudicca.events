plugins {
    id("boudicca-kotlin")
}

dependencies {
    api("com.microsoft.playwright:playwright:1.42.0")
    api("com.deque.html.axe-core:playwright:4.8.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}