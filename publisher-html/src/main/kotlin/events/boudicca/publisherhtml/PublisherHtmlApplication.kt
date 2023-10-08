package events.boudicca.publisherhtml

import com.github.jknack.handlebars.ValueResolver
import events.boudicca.publisherhtml.extension.HeaderExtensionValueResolver
import events.boudicca.publisherhtml.handlebars.HandlebarsViewResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.ViewResolver


@SpringBootApplication
@EnableScheduling
class PublisherHtmlApplication {
    @Bean
    fun handlebarsViewResolver(headerExtensionValueResolver: HeaderExtensionValueResolver): ViewResolver {
        val valueResolvers = ValueResolver.defaultValueResolvers().union(listOf(headerExtensionValueResolver))

        val viewResolver = HandlebarsViewResolver()
        viewResolver.setValueResolvers(*valueResolvers.toTypedArray())
        viewResolver.setPrefix("classpath:/templates")
        return viewResolver
    }

}

fun main(args: Array<String>) {
    runApplication<PublisherHtmlApplication>(*args)
}
