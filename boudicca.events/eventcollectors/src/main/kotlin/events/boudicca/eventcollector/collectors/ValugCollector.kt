package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.model.Event
import base.boudicca.model.EventCategory

/**
 * VorAlpen Linux User Group
 */
class ValugCollector : IcalCollector("valug") {

    private val fetcher = Fetcher()
    private val baseUrl = "https://valug.at/"
    private val icsUrl = "${baseUrl}events/index.ics"

    override fun getAllIcalResources(): List<String> {
        return listOf(fetcher.fetchUrl(icsUrl))
    }

    override fun postProcess(event: Event): Event {
        return Event(event.name, event.startDate,
            event.data.toMutableMap().apply {
                put(SemanticKeys.TAGS, listOf("VALUG", "Linux", "User Group").toString())
                put(SemanticKeys.CATEGORY, EventCategory.TECH.name)
                put(SemanticKeys.REGISTRATION, "free")
                put(SemanticKeys.SOURCES, "${icsUrl}\n${baseUrl}")
                put(SemanticKeys.TYPE, "techmeetup") // TODO same as with Technologieplauscherl
            })
    }
}
