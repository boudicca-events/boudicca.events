package base.boudicca.search

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@OpenAPIDefinition(
    servers = [
        Server(url = "/", description = "Default Server URL")
    ],
    info = Info(
        title = "Boudicca EventDB",
        version = "0.1",
        license = License(
            name = "GPL v3"
        )
    ),
)
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BoudiccaSearchProperties::class)
class SearchApplication

fun main(args: Array<String>) {
    runApplication<SearchApplication>(*args)
}
