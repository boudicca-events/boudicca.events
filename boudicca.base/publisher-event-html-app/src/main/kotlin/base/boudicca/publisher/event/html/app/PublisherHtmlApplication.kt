package base.boudicca.publisher.event.html.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PublisherHtmlApplication

fun main(args: Array<String>) {
    runApplication<PublisherHtmlApplication>(*args)
}
