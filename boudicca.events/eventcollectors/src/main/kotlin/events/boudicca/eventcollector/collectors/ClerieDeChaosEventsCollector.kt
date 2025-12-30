package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.splitAtNewline
import base.boudicca.api.eventcollector.util.tryParseToUriOrNull
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Händisch zusammengesuchte Chaosnahe Events
 */
@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["chaosevents_clerie_de"])
class ClerieDeChaosEventsCollector : IcalCollector("chaosevents.clerie.de") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://chaosevents.clerie.de/"
    private val icsUrl = "${baseUrl}chaosevents.ics"

    override fun getAllIcalResources(): List<String> = listOf(fetcher.fetchUrl(icsUrl))

    override fun postProcess(event: StructuredEvent): StructuredEvent {
        val eventUrl =
            event
                .getProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY)
                .firstOrNull()
                ?.second
                ?.splitAtNewline()
                ?.findFirstParsableUrl()

        val structuredEvent =
            event
                .toBuilder()
                .withProperty(
                    SemanticKeys.TAGS_PROPERTY,
                    listOf("Chaos", "CCC", "tech", "privacy", "hacking", "making", "programming"),
                ).withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.TECH)
                .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl, icsUrl))
                .withProperty(SemanticKeys.TYPE_PROPERTY, "chaosevent")

        if (eventUrl != null) {
            structuredEvent.withProperty(SemanticKeys.URL_PROPERTY, eventUrl)
        }
        return structuredEvent.build()
    }

    private fun List<String>.findFirstParsableUrl(): URI? =
        this
            .asSequence()
            .map { it.replace("* ", "") } // parse away list formatting that is added sometimes
            .map { it.replace("- ", "") }
            .filter(String::isNotBlank)
            .filter { it.startsWith("http://") || it.startsWith("https://") }
            .map(String::tryParseToUriOrNull)
            .filterNotNull()
            .firstOrNull()
}
