package base.boudicca.api.eventdb.publisher

import base.boudicca.model.Entry
import base.boudicca.model.Event
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.ApiException
import events.boudicca.openapi.api.PublisherResourceApi

class EventDB(eventDbUrl: String) {

    private val publisherApi: PublisherResourceApi

    init {
        if (eventDbUrl.isBlank()) {
            throw IllegalStateException("you need to pass an eventDbUrl!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(eventDbUrl)
        publisherApi = PublisherResourceApi(apiClient)
    }

    fun getAllEvents(): Set<Event> {
        return getAllEntries().mapNotNull { Event.fromEntry(it) }.toSet()
    }

    fun getAllEntries(): Set<Entry> {
        try {
            return publisherApi.all()
        } catch (e: ApiException) {
            throw EventDBException("could not reach eventdb", e)
        }
    }
}

class EventDBException(msg: String, e: ApiException) : RuntimeException(msg ,e)