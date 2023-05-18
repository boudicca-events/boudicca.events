package events.boudicca.publisherhtml.service

import events.boudicca.EventTypes
import events.boudicca.SemanticKeys
import events.boudicca.search.openapi.ApiClient
import events.boudicca.search.openapi.api.SearchResourceApi
import events.boudicca.search.openapi.model.Event
import events.boudicca.search.openapi.model.SearchDTO
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class EventService {
    private val searchApi: SearchResourceApi
    private val rows: Int = 30

    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'")

    init {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(autoDetectUrl())
        searchApi = SearchResourceApi(apiClient)
    }

    fun getAllEvents(): List<Map<String, String?>> {
        return mapEvents(searchApi.searchPost(SearchDTO()))
    }

    fun search(searchDTO: SearchDTO, offset: Int): List<Map<String, String?>> {
        return mapEvents(searchApi.searchPost(searchDTO), offset)
    }

    fun getLocationNames(): List<String> {
        return searchApi.searchPost(SearchDTO())
            .mapNotNull { it.data?.get(SemanticKeys.LOCATION_NAME) }
            .filter { it.isNotBlank() }
            .toSet()
            .sortedBy { it }
    }

    fun getLocationCities(): List<String> {
        return searchApi.searchPost(SearchDTO())
            .mapNotNull { it.data?.get(SemanticKeys.LOCATION_CITY) }
            .filter { it.isNotBlank() }
            .toSet()
            .sortedBy { it }
    }

    fun getAllTypes(): List<String> {
        val types = EventTypes.values().map { frontEndTypeName(it) }.toMutableList()
        types.add(0, "Alle")
        types.add("Andere")
        return types
    }

    private fun mapEvents(
        events: List<Event>,
        offset: Int = 0
    ): List<Map<String, String?>> {
        return events.asSequence()
            .filter { it.startDate.isAfter(OffsetDateTime.now().minusDays(1)) }
            .drop(offset).take(rows)
            .map { mapEvent(it) }
            .toList()
    }

    private fun mapEvent(event: Event): Map<String, String?> {
        return mapOf(
            "name" to event.name,
            "description" to event.data?.get(SemanticKeys.DESCRIPTION),
            "url" to event.data?.get(SemanticKeys.URL),
            "startDate" to formatDate(event.startDate),
            "locationName" to (event.data?.get(SemanticKeys.LOCATION_NAME) ?: "unbekannt"),
            "city" to event.data?.get(SemanticKeys.LOCATION_CITY),
            "type" to mapType(event.data?.get(SemanticKeys.TYPE)),
            "pictureUrl" to (event.data?.get("pictureUrl") ?: ""),
        )
    }

    private fun mapType(type: String?): String? {
        if (type === null) {
            return null
        }

        val lowerCaseType = type.lowercase()
        for (eventType in EventTypes.values()) {
            if (eventType.types.contains(lowerCaseType)) {
                return frontEndTypeName(eventType)
            }
        }

        return null
    }

    private fun formatDate(startDate: OffsetDateTime): String {
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
        return "http://localhost:8082"
    }

    private fun frontEndTypeName(type: EventTypes): String {
        return when (type) {
            EventTypes.MUSIC -> "Musik"
            EventTypes.ART -> "Kunst"
            EventTypes.TECH -> "Technologie"
        }
    }
}

