package events.boudicca.enricher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class EnricherApplication

fun main(args: Array<String>) {
    runApplication<EnricherApplication>(*args)
}
