package base.boudicca.publisher.event.ical

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class IcalPublisherApplication

fun main(args: Array<String>) {
    runApplication<IcalPublisherApplication>(*args)
}
