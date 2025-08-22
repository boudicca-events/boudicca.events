plugins {
    id("boudicca-springboot-lib")
}

dependencies {
    api(project(":boudicca.base:search-client"))
    api(libs.spring.boot.starter.web)
    api(libs.handlebars)
    api(libs.handlebars.springmvc)
    implementation(libs.bundles.twelvemonkeys)
    implementation(libs.otel.java.httpclient)
    testImplementation(libs.microsoft.playwright)
    testImplementation(libs.axe.core.playwright)
}
