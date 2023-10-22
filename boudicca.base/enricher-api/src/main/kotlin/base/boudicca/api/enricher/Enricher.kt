package base.boudicca.api.enricher

import base.boudicca.Event
import events.boudicca.enricher.openapi.ApiClient
import events.boudicca.enricher.openapi.ApiException
import events.boudicca.enricher.openapi.api.EnricherControllerApi
import events.boudicca.enricher.openapi.model.EnrichRequestDTO

class Enricher(enricherUrl: String) {

    private val enricherApi: EnricherControllerApi

    init {
        if (enricherUrl.isBlank()) {
            throw IllegalStateException("you need to pass an eventDbUrl!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(enricherUrl)

        enricherApi = EnricherControllerApi(apiClient)
    }

    fun enrichEvents(events: List<Event>): List<Event> {
        try {
            return enricherApi.enrich(EnrichRequestDTO().events(events.map { mapToEnricherEvent(it) }))
                .map { toEvent(it) }
        } catch (e: ApiException) {
            throw EnricherException("could not reach eventdb", e)
        }
    }

    private fun toEvent(enricherEvent: events.boudicca.enricher.openapi.model.Event): Event {
        return Event(enricherEvent.name, enricherEvent.startDate, enricherEvent.data ?: mapOf())
    }

    private fun mapToEnricherEvent(event: Event): events.boudicca.enricher.openapi.model.Event {
        return events.boudicca.enricher.openapi.model.Event()
            .name(event.name)
            .startDate(event.startDate)
            .data(event.data)
    }
}

class EnricherException(msg: String, e: ApiException) : RuntimeException(msg, e)