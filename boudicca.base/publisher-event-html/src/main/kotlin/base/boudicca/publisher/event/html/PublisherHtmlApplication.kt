package base.boudicca.publisher.event.html

import base.boudicca.publisher.event.html.handlebars.HandlebarsViewResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.ViewResolver


@SpringBootApplication
@EnableScheduling
class PublisherHtmlApplication {
    @Bean
    fun handlebarsViewResolver(): ViewResolver {
        val viewResolver = HandlebarsViewResolver()
        viewResolver.setPrefix("classpath:/templates")
        return viewResolver
    }
}

fun main(args: Array<String>) {
    runApplication<PublisherHtmlApplication>(*args)
}
