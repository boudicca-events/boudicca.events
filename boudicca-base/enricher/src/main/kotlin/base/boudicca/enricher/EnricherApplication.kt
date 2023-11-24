package base.boudicca.enricher

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@OpenAPIDefinition(
    servers = [
        Server(url = "/", description = "Default Server URL")
    ],
    info = Info(
        title = "Boudicca Enricher",
        version = "0.1",
        license = License(
            name = "GPL v3"
        )
    ),
)
@SpringBootApplication
@EnableScheduling
class EnricherApplication

fun main(args: Array<String>) {
    runApplication<EnricherApplication>(*args)
}
