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
    private val rows: Int = 30

    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'")

    private val miscArtTypes = arrayOf(
        "kabarett", "theater", "wissenskabarett", "provinzkrimi",
        "comedy", "figurentheater", "film", "visual comedy", "tanz", "performance"
    );
    private val musicTypes =
        arrayOf("konzert", "concert", "alternative", "singer/songwriter", "party", "songwriter/alternative");
    private val techTypes = arrayOf("techmeetup");

    init {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(autoDetectUrl())
        publisherApi = EventPublisherResourceApi(apiClient)
    }

    fun getAllEvents(): List<Map<String, String?>> {
        return mapEvents(publisherApi.eventsGet())
    }

    fun search(searchDTO: SearchDTO, offset: Int, filterDTO: FilterDTO = FilterDTO()): List<Map<String, String?>> {
        return mapEvents(publisherApi.eventsSearchPost(searchDTO), offset, filterDTO)
    }

    fun getLocationNames(): List<String> {
        return publisherApi.eventsGet()
            .mapNotNull { it.data?.get(SemanticKeys.LOCATION_NAME) }
            .filter { it.isNotBlank() }
            .toSet()
            .sortedBy { it }
    }

    fun getLocationCities(): List<String> {
        return publisherApi.eventsGet()
            .mapNotNull { it.data?.get(SemanticKeys.LOCATION_CITY) }
            .filter { it.isNotBlank() }
            .toSet()
            .sortedBy { it }
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

