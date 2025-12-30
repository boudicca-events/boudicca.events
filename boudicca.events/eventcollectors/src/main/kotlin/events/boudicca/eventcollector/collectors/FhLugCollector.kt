package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.EventCategory
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*

/**
 * fhLUG: Fachhochschulcampus Hagenberg Linux User Group
 */
@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["fhLUG"])
class FhLugCollector : IcalCollector("fhLUG") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://fhlug.at/"
    private val icsUrl = "${baseUrl}events/events.ics"

    override fun getAllIcalResources(): List<String> = listOf(fetcher.fetchUrl(icsUrl))

    override fun postProcess(event: StructuredEvent): StructuredEvent {
        val builder =
            event
                .toBuilder()
                .withProperty(SemanticKeys.TAGS_PROPERTY, listOf("fhLUG", "Linux", "User Group", "Free Software"))
                .withProperty(SemanticKeys.TYPE_PROPERTY, "techmeetup") // TODO same as with Technologieplauscherl
                .withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.TECH)
                .withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.FREE)
                .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(icsUrl, baseUrl))

        if (event
                .getProperty(SemanticKeys.URL_PROPERTY)
                .isEmpty() &&
            event.getProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY).isNotEmpty()
        ) {
            val url = tryGetUrlFromDescription(event.getProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY).first().second)
            if (url.isPresent) {
                builder.withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url.get()))
            }
        }

        return builder.build()
    }

    private fun tryGetUrlFromDescription(description: String): Optional<String> {
        val document = Jsoup.parse(description)
        val href = document.select("a").first()?.attr("href")
        if (href?.startsWith("http") == true) {
            return Optional.of(href)
        }

        if (description.startsWith("http")) {
            return Optional.of(description)
        }

        return Optional.empty()
    }
}
