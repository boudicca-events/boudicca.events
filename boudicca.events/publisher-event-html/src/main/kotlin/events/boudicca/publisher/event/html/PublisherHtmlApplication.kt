package events.boudicca.publisher.event.html

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class PublisherHtmlApplication

fun main(args: Array<String>) {
    runApplication<PublisherHtmlApplication>(*args)
}
