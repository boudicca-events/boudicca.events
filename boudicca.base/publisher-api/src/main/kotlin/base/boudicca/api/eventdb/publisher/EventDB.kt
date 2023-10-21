package base.boudicca.api.eventdb.publisher

import base.boudicca.Event
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventPublisherResourceApi

class EventDB(eventDbUrl: String) {

    private val publisherApi: EventPublisherResourceApi

    init {
        if (eventDbUrl.isBlank()) {
            throw IllegalStateException("you need to pass an eventDbUrl!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(eventDbUrl)
        publisherApi = EventPublisherResourceApi(apiClient)
    }

    fun getAllEvents(): List<Event> {
        val events = publisherApi.eventsGet()

        return events.map { Event(it.name, it.startDate, it.data ?: emptyMap()) }
    }
}