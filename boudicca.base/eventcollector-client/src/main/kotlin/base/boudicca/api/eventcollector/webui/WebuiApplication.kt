package base.boudicca.api.eventcollector.webui

import base.boudicca.springboot.common.MonitoringConfiguration
import com.github.jknack.handlebars.helper.ConditionalHelpers
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.ViewResolver

@SpringBootApplication(
    exclude = [
        // is manually setup in EventCollectionCoordinator
        MonitoringConfiguration::class,
    ],
)
class WebuiApplication {
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
