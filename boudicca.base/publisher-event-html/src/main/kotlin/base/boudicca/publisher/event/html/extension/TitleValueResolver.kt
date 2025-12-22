package base.boudicca.publisher.event.html.extension

import base.boudicca.publisher.event.html.PublisherHtmlProperties
import com.github.jknack.handlebars.ValueResolver
import org.springframework.stereotype.Component

@Component
class TitleValueResolver(private val properties: PublisherHtmlProperties) : ValueResolver {
    override fun resolve(context: Any?, name: String?): Any = when (name) {
        "pageTitle" -> {
            properties.pageTitle
        }

        "headerTitle" -> {
            properties.headerTitle
        }

        else -> {
            ValueResolver.UNRESOLVED
        }
    }

    override fun resolve(context: Any?): Any = ValueResolver.UNRESOLVED

    override fun propertySet(context: Any?) = mutableMapOf(
        "pageTitle" to properties.pageTitle,
        "headerTitle" to properties.headerTitle,
    ).entries
}
