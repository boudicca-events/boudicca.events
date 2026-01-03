package base.boudicca.api.eventcollector.configuration

import com.github.jknack.handlebars.helper.ConditionalHelpers
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver

@Configuration
class EventCollectorConfiguration {
    @Bean
    fun handlebarsViewResolver(): ViewResolver {
        val viewResolver = HandlebarsViewResolver()
        viewResolver.order = 0 // we have to decrease the order so ours is first (default is Int.MAX_VALUE)
        viewResolver.setFailOnMissingFile(false)

        for (helper in ConditionalHelpers.entries) {
            viewResolver.registerHelper(helper.name, helper)
        }

        viewResolver.setPrefix("classpath:/templates")

        return viewResolver
    }
}
