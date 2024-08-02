plugins {
    id("boudicca-springboot-app")
}

dependencies {
    implementation(project(":boudicca.base:semantic-conventions"))
    implementation(project(":boudicca.base:search-client"))
    implementation("net.sf.biweekly:biweekly:0.6.8")
}
