package base.boudicca.publisher.event.html

import base.boudicca.publisher.event.html.extension.LinkExtensionValueResolver
import base.boudicca.publisher.event.html.extension.TitleValueResolver
import com.github.jknack.handlebars.ValueResolver
import com.github.jknack.handlebars.cache.NullTemplateCache
import com.github.jknack.handlebars.helper.ConditionalHelpers
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@EnableScheduling
@ComponentScan
@EnableConfigurationProperties(PublisherHtmlProperties::class)
@AutoConfiguration
class PublisherHtmlConfiguration(private val properties: PublisherHtmlProperties) : WebMvcConfigurer {
    @Bean
    fun handlebarsViewResolver(
        headerExtensionValueResolver: LinkExtensionValueResolver,
        titleValueResolver: TitleValueResolver
    ): ViewResolver {
        val viewResolver = HandlebarsViewResolver()
        viewResolver.order = 0

        for (helper in ConditionalHelpers.entries) {
            viewResolver.registerHelper(helper.name, helper)
        }

        viewResolver.setPrefix("classpath:/templates")
        viewResolver.setFailOnMissingFile(false)

        if (properties.devMode) {
            viewResolver.setCacheFilter { _, _, _ -> false }
            viewResolver.setTemplateCache(NullTemplateCache.INSTANCE)
            viewResolver.setPrefix("file:boudicca.base/publisher-event-html/src/main/resources/templates/")
        }

        val valueResolvers =
            ValueResolver.defaultValueResolvers().union(listOf(headerExtensionValueResolver, titleValueResolver))
        viewResolver.setValueResolvers(*valueResolvers.toTypedArray())
        return viewResolver
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        if (properties.devMode) {
            registry
                .addResourceHandler("/**")
                .addResourceLocations("file:boudicca.base/publisher-event-html/src/main/resources/static/")
        }
    }
}
