package base.boudicca.api.eventcollector.webui

import com.github.jknack.handlebars.helper.ConditionalHelpers
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.ViewResolver

@SpringBootApplication
class WebuiApplication {
    @Bean
    fun handlebarsViewResolver(): ViewResolver {
        val viewResolver = HandlebarsViewResolver()
        viewResolver.order = 0 //we have to decrease the order so ours is first (default is Int.MAX_VALUE)

        for (helper in ConditionalHelpers.entries) {
            viewResolver.registerHelper(helper.name, helper)
        }

        viewResolver.setPrefix("classpath:/templates")

        return viewResolver
    }
}
