package base.boudicca.eventdb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventDBApplication

fun main(args: Array<String>) {
    runApplication<EventDBApplication>(*args)
}
