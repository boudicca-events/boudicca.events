package events.boudicca.publisherhtml.service

import events.boudicca.SemanticKeys
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventPublisherResourceApi
import events.boudicca.openapi.model.Event
import events.boudicca.openapi.model.SearchDTO
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class EventService {
    private val publisherApi: EventPublisherResourceApi
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'")

    init {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(autoDetectUrl())
        publisherApi = EventPublisherResourceApi(apiClient)
    }

    fun getAllEvents(): Set<Map<String, String?>> {
        return publisherApi.eventsGet().map{map(it)}.toSet()
    }

    fun search(searchDTO: SearchDTO): Set<Map<String, String?>> {
        return publisherApi.eventsSearchPost(searchDTO).map{map(it)}.toSet()
    }

    fun map(event: Event): Map<String, String?> {
        return mapOf(
                "name" to event.name,
                "startDate" to formatDate(event.startDate),
                "url" to (event.data?.get(SemanticKeys.URL) ?: ""),
                "locationName" to (event.data?.get(SemanticKeys.LOCATION_NAME) ?: "unbekannt"),
                "city" to (event.data?.get(SemanticKeys.LOCATION_CITY) ?: ""),
        )
    }

    fun formatDate(startDate: OffsetDateTime): String {
        return formatter.format(startDate);
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