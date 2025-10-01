package base.boudicca.publisher.event.html.extension

import com.github.jknack.handlebars.ValueResolver
import org.springframework.stereotype.Component

@Component
class LinkExtensionValueResolver(extensions: List<Extension>) : ValueResolver {

    private val headers: List<Map<String, String>> = getAllExtensionHeaders(extensions)
    private val footers: List<Map<String, String>> = getAllExtensionFooters(extensions)

    override fun resolve(context: Any?, name: String?): Any {
        return if (name == "extensionHeaders") {
            headers
        } else if (name == "extensionFooters") {
            footers
        } else {
            ValueResolver.UNRESOLVED
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
                    "target" to header.target,
                    "svgName" to header.svgName,
                )
            }
    }

    private fun getAllExtensionFooters(extensions: List<Extension>): List<Map<String, String>> {
        return extensions.flatMap { extension -> extension.getFooters() }
            .map { header ->
                mapOf(
                    "text" to header.text,
                    "url" to header.url,
                    "target" to header.target,
                    "svgName" to header.svgName
                )
            }
    }

}
