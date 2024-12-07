package base.boudicca.publisher.event.html

import base.boudicca.publisher.event.html.extension.HeaderExtensionValueResolver
import base.boudicca.publisher.event.html.handlebars.HandlebarsViewResolver
import com.github.jknack.handlebars.ValueResolver
import com.github.jknack.handlebars.cache.NullTemplateCache
import com.github.jknack.handlebars.helper.ConditionalHelpers
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PublisherHtmlProperties::class)
class PublisherHtmlApplication(private val properties: PublisherHtmlProperties) : WebMvcConfigurer {
    @Bean
    fun handlebarsViewResolver(
        headerExtensionValueResolver: HeaderExtensionValueResolver
    ): ViewResolver {
        val viewResolver = HandlebarsViewResolver()

        for (helper in ConditionalHelpers.entries) {
            viewResolver.registerHelper(helper.name, helper)
        }

        viewResolver.setPrefix("classpath:/templates")
        
        if (properties.devMode) {
            viewResolver.setCacheFilter { _, _, _ -> false }
            viewResolver.setTemplateCache(NullTemplateCache.INSTANCE)
            viewResolver.setPrefix("file:boudicca.base/publisher-event-html/src/main/resources/templates/")
        }

        val valueResolvers = ValueResolver.defaultValueResolvers().union(listOf(headerExtensionValueResolver))
        viewResolver.setValueResolvers(*valueResolvers.toTypedArray())
        return viewResolver
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        if(properties.devMode){
            registry
                .addResourceHandler("/**")
                .addResourceLocations("file:boudicca.base/publisher-event-html/src/main/resources/static/")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<PublisherHtmlApplication>(*args)
}
