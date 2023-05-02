package events.boudicca.publisherhtml.service

import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventPublisherResourceApi
import events.boudicca.openapi.model.Event
import events.boudicca.openapi.model.SearchDTO
import org.springframework.stereotype.Service

@Service
class EventService {
    private val publisherApi: EventPublisherResourceApi

    init {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(autoDetectUrl())
        publisherApi = EventPublisherResourceApi(apiClient)
    }

    fun getAllEvents(): Set<Event> {
        return publisherApi.eventsGet()
    }

    fun search(searchDTO: SearchDTO): Set<Event> {
        return publisherApi.eventsSearchPost(searchDTO)
    }

    private fun autoDetectUrl(): String {
        var url = System.getenv("BOUDICCA_URL")
        if (url != null && url.isNotBlank()) {
            return url
        }
        url = System.getProperty("boudiccaUrl")
        if (url != null && url.isNotBlank()) {
            return url
        }
        return "http://localhost:8081"
    }
}