package base.boudicca.api.eventdb.publisher

import base.boudicca.Entry
import base.boudicca.Event
import events.boudicca.openapi.ApiClient
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
        return publisherApi.all()
    }
}