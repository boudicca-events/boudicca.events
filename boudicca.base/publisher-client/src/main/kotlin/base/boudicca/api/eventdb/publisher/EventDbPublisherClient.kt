package base.boudicca.api.eventdb.publisher

import base.boudicca.eventdb.openapi.api.PublisherApi
import base.boudicca.model.Entry
import base.boudicca.model.Event
import base.boudicca.model.toEvent
import base.boudicca.openapi.ApiClient
import base.boudicca.openapi.ApiException
import kotlin.jvm.optionals.getOrNull

class EventDbPublisherClient(private val eventDbUrl: String) {

    private val publisherApi: PublisherApi

    init {
        if (eventDbUrl.isBlank()) {
            throw IllegalStateException("you need to pass an eventDbUrl!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(eventDbUrl)
        publisherApi = PublisherApi(apiClient)
    }

    fun getAllEvents(): Set<Event> {
        return getAllEntries().mapNotNull { it.toEvent() }.toSet()
    }

    fun getAllEntries(): Set<Entry> {
        try {
            return publisherApi.all()
        } catch (e: ApiException) {
            throw EventDBException("could not reach eventdb: $eventDbUrl", e)
        }
    }
}

class EventDBException(msg: String, e: ApiException) : RuntimeException(msg, e)
