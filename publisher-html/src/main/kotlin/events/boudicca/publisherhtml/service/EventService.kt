package events.boudicca.publisherhtml.service

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

    fun search(searchDTO: SearchDTO, offset: Int, filterDTO: FilterDTO = FilterDTO()): List<Map<String, String?>> {
        return mapEvents(searchApi.searchPost(searchDTO), offset, filterDTO)
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
        val types = EventTypes.values().map { it.frontEndName }.toMutableList()
        types.add(0, "Alle")
        types.add("Andere")
        return types
    }

    private fun mapEvents(
        events: Set<Event>,
        offset: Int = 0,
        filterDTO: FilterDTO = FilterDTO()
    ): List<Map<String, String?>> {
        return events.asSequence()
            .filter { it.startDate.isAfter(OffsetDateTime.now().minusDays(1)) }
            .filter { matchesFilter(it, filterDTO) }
            .sortedBy { it.startDate }
            .drop(offset).take(rows)
            .map { mapEvent(it) }
            .toList()
    }

    private fun matchesFilter(event: Event, filterDTO: FilterDTO): Boolean {
        if (!filterDTO.type.isNullOrBlank()) {
            if (!matchTypeFilter(filterDTO.type, event)) {
                return false
            }
        }
        if (!filterDTO.locationName.isNullOrBlank()
            && event.data?.get(SemanticKeys.LOCATION_NAME) != filterDTO.locationName
        ) {
            return false
        }
        if (!filterDTO.locationCity.isNullOrBlank()
            && event.data?.get(SemanticKeys.LOCATION_CITY) != filterDTO.locationCity
        ) {
            return false
        }
        return true
    }

    private fun matchTypeFilter(type: String, event: Event): Boolean {
        if (type == "Alle") {
            return true
        }

        val lowerCaseType = event.data?.get(SemanticKeys.TYPE)?.lowercase() ?: ""
        val eventType = EventTypes.values().firstOrNull { it.types.contains(lowerCaseType) }
        if (eventType == null) {
            return type == "Andere"
        }
        return eventType.frontEndName == type
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
                return eventType.frontEndTypeName
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

    enum class EventTypes(
        val frontEndName: String,
        val frontEndTypeName: String,
        val types: Set<String>,
    ) {
        MUSIC(
            "Musik", "music",
            setOf("konzert", "concert", "alternative", "singer/songwriter", "party", "songwriter/alternative")
        ),
        ART(
            "Kunst", "miscArt",
            setOf(
                "kabarett", "theater", "wissenskabarett", "provinzkrimi", "comedy", "figurentheater", "film",
                "visual comedy", "tanz", "performance", "musiklesung", "literatur"
            )
        ),
        TECH(
            "Technologie", "tech",
            setOf("techmeetup")
        ),
    }
}

