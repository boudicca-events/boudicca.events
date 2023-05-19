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
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'")

    init {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(autoDetectUrl())
        searchApi = SearchResourceApi(apiClient)
    }

    fun getAllEvents(): List<Map<String, String?>> {
        return mapEvents(searchApi.searchPost(SearchDTO()))
    }

    fun search(searchDTO: SearchDTO): List<Map<String, String?>> {
        return mapEvents(searchApi.searchPost(searchDTO))
    }

    fun filters(): Filters {
        val filters = searchApi.filtersGet()
        return Filters(
            filters.types.map { Pair(it, frontEndName(it)) }.sortedBy { it.second },
            filters.locationNames.sorted().map { Pair(it, it) },
            filters.locationCities.sorted().map { Pair(it, it) },
        )
    }


    private fun mapEvents(events: List<Event>): List<Map<String, String?>> {
        return events.map { mapEvent(it) }
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
            EventTypes.MUSIC -> "music"
            EventTypes.ART -> "miscArt"
            EventTypes.TECH -> "tech"
        }
    }

    private fun frontEndName(type: String): String {
        return when (type) {
            "MUSIC" -> "Musik"
            "ART" -> "Kunst"
            "TECH" -> "Technologie"
            "ALL" -> "Alle"
            "OTHER" -> "Andere"
            else -> "???"
        }
    }

    data class Filters(
        val types: List<Pair<String, String>>,
        val locationNames: List<Pair<String, String>>,
        val locationCities: List<Pair<String, String>>,
    )
}

