package events.boudicca.publisherhtml

import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.ViewResolver


@SpringBootApplication
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
