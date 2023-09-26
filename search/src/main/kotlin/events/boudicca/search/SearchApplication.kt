package events.boudicca.search

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@SpringBootApplication
@EnableScheduling
class SearchApplication {
}

fun main(args: Array<String>) {
    runApplication<SearchApplication>(*args)
}
