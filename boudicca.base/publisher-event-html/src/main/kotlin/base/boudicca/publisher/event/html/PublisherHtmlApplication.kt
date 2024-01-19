package base.boudicca.publisher.event.html

import base.boudicca.publisher.event.html.extension.HeaderExtensionValueResolver
import base.boudicca.publisher.event.html.handlebars.HandlebarsViewResolver
import com.github.jknack.handlebars.ValueResolver
import com.github.jknack.handlebars.helper.ConditionalHelpers
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
        val viewResolver = HandlebarsViewResolver()

        for (helper in ConditionalHelpers.entries) {
            viewResolver.registerHelper(helper.name, helper)
        }

        viewResolver.setPrefix("classpath:/templates")

        val valueResolvers = ValueResolver.defaultValueResolvers().union(listOf(headerExtensionValueResolver))
        viewResolver.setValueResolvers(*valueResolvers.toTypedArray())
        return viewResolver
    }
}

fun main(args: Array<String>) {
    runApplication<PublisherHtmlApplication>(*args)
}
