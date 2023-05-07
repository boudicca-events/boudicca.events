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

    private val miscArtTypes = arrayOf("kabarett", "theater", "wissenskabarett", "provinzkrimi",
            "comedy", "figurentheater", "film", "visual comedy", "tanz", "performance");
    private val musicTypes = arrayOf("concert", "alternative", "singer/songwriter", "party", "songwriter/alternative");
    private val techTypes = arrayOf("techmeetup");

    init {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(autoDetectUrl())
        publisherApi = EventPublisherResourceApi(apiClient)
    }

    fun getAllEvents(): Set<Map<String, String?>> {
        return publisherApi.eventsGet().map { map(it) }.toSet()
    }

    fun search(searchDTO: SearchDTO): Set<Map<String, String?>> {
        return publisherApi.eventsSearchPost(searchDTO).map { map(it) }.toSet()
    }

    fun map(event: Event): Map<String, String?> {
        return mapOf(
                "name" to event.name,
                "description" to event.data?.get(SemanticKeys.DESCRIPTION),
                "url" to event.data?.get(SemanticKeys.URL),
                "startDate" to formatDate(event.startDate),
                "locationName" to (event.data?.get(SemanticKeys.LOCATION_NAME) ?: "unbekannt"),
                "city" to event.data?.get(SemanticKeys.LOCATION_CITY),
                "type" to mapType(event.data?.get(SemanticKeys.TYPE))
        )
    }

    fun mapType(type: String?): String? {
        if (type === null) {
            return null
        }

        val lowerCaseType = type.lowercase();
        if (miscArtTypes.contains(lowerCaseType)) {
            return "miscArt"
        } else if (musicTypes.contains(lowerCaseType)) {
            return "music"
        } else if (techTypes.contains(lowerCaseType)) {
            return "tech"
        }

        return null
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