package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import org.jsoup.Jsoup
import java.util.*

/**
 * fhLUG: Fachhochschulcampus Hagenberg Linux User Group
 */
class FhLugCollector : IcalCollector("fhLUG") {

    private val fetcher = Fetcher()
    private val baseUrl = "https://fhlug.at/"
    private val icsUrl = "${baseUrl}events/events.ics"

    override fun getAllIcalResources(): List<String> {
        return listOf(fetcher.fetchUrl(icsUrl))
    }

    override fun postProcess(event: Event): Event {
        return Event(event.name, event.startDate,
            event.data.toMutableMap().apply {
                put(SemanticKeys.TAGS, listOf("fhLUG", "Linux", "User Group", "Free Software").toString())
                if (!event.data.containsKey(SemanticKeys.URL) && event.data.containsKey(SemanticKeys.DESCRIPTION)) {
                    val url = tryGetUrlFromDescription(event.data[SemanticKeys.DESCRIPTION]!!)
                    if (url.isPresent) {
                        put(SemanticKeys.URL, url.get())
                    }
                }
                put(SemanticKeys.TYPE, "techmeetup") // TODO same as with Technologieplauscherl
                put(SemanticKeys.CATEGORY, EventCategory.TECH.name)
                put(SemanticKeys.REGISTRATION, "free")
                put(SemanticKeys.SOURCES, "${icsUrl}\n${baseUrl}")
            })
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
