package events.boudicca.publisherhtml.extension

import com.github.jknack.handlebars.ValueResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class HeaderExtensionValueResolver @Autowired constructor(extensions: List<Extension>) : ValueResolver {

    private val headers: List<Map<String, String>> = getAllExtensionHeaders(extensions)

    override fun resolve(context: Any?, name: String?): Any {
        if (name == "extensionHeaders") {
            return headers
        } else {
            return ValueResolver.UNRESOLVED
        }
    }

    override fun resolve(context: Any?): Any {
        return ValueResolver.UNRESOLVED
    }

    override fun propertySet(context: Any?): MutableSet<MutableMap.MutableEntry<String, List<Map<String, String>>>> {
        return mutableSetOf(mutableMapOf("extensionHeaders" to headers).entries.first())
    }

    private fun getAllExtensionHeaders(extensions: List<Extension>): List<Map<String, String>> {
        return extensions.flatMap { extension -> extension.getHeaders() }
            .map { header ->
                mapOf(
                    "text" to header.text,
                    "url" to header.url,
                )
            }
    }

}