package events.boudicca.search

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@OpenAPIDefinition(
    servers = [
        Server(url = "/", description = "Default Server URL")
    ]
)
@SpringBootApplication
@EnableScheduling
class SearchApplication {
}

fun main(args: Array<String>) {
    runApplication<SearchApplication>(*args)
}
