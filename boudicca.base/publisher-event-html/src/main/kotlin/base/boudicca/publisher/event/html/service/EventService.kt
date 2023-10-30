package base.boudicca.publisher.event.html.service

import base.boudicca.Event
import base.boudicca.EventCategory
import base.boudicca.SemanticKeys
import base.boudicca.api.search.BoudiccaQueryBuilder.after
import base.boudicca.api.search.BoudiccaQueryBuilder.and
import base.boudicca.api.search.BoudiccaQueryBuilder.before
import base.boudicca.api.search.BoudiccaQueryBuilder.contains
import base.boudicca.api.search.BoudiccaQueryBuilder.durationLonger
import base.boudicca.api.search.BoudiccaQueryBuilder.durationShorter
import base.boudicca.api.search.BoudiccaQueryBuilder.equals
import base.boudicca.api.search.QueryDTO
import base.boudicca.api.search.Search
import base.boudicca.api.search.SearchResultDTO
import base.boudicca.publisher.event.html.model.SearchDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

//we do not want long-running events on our site, so we filter for events short then 30 days
const val DEFAULT_DURATION_SHORTER_VALUE = 24 * 30

private const val SEARCH_TYPE_ALL = "ALL"

@Service
class EventService @Autowired constructor(@Value("\${boudicca.search.url}") private val searchUrl: String) {
    private val search: Search = createSearchApi()
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'", Locale.GERMAN)
    private val localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun search(searchDTO: SearchDTO): List<Map<String, String?>> {
        val events = search.queryEvents(QueryDTO(generateQuery(searchDTO), searchDTO.offset ?: 0))
        return mapEvents(events)
    }

    fun generateQuery(searchDTO: SearchDTO): String {
        val name = searchDTO.name
        val query = if (name != null && name.startsWith('!')) {
            name.substring(1)
        } else {
            setDefaults(searchDTO)
            buildQuery(searchDTO)
        }
        return query
    }

    private fun buildQuery(searchDTO: SearchDTO): String {
        val queryParts = mutableListOf<String>()
        if (!searchDTO.name.isNullOrBlank()) {
            queryParts.add(contains("*", searchDTO.name!!))
        }
        if (!searchDTO.category.isNullOrBlank() && searchDTO.category != SEARCH_TYPE_ALL) {
            queryParts.add(equals(SemanticKeys.CATEGORY, searchDTO.category!!))
        }
        if (!searchDTO.locationCity.isNullOrBlank()) {
            queryParts.add(equals(SemanticKeys.LOCATION_CITY, searchDTO.locationCity!!))
        }
        if (!searchDTO.locationName.isNullOrBlank()) {
            queryParts.add(equals(SemanticKeys.LOCATION_NAME, searchDTO.locationName!!))
        }
        if (!searchDTO.fromDate.isNullOrBlank()) {
            queryParts.add(after(SemanticKeys.STARTDATE, LocalDate.parse(searchDTO.fromDate!!, localDateFormatter)))
        }
        if (!searchDTO.toDate.isNullOrBlank()) {
            queryParts.add(before(SemanticKeys.STARTDATE, LocalDate.parse(searchDTO.toDate!!, localDateFormatter)))
        }
        if (searchDTO.durationShorter != null) {
            queryParts.add(durationShorter(SemanticKeys.STARTDATE, SemanticKeys.ENDDATE, searchDTO.durationShorter!!))
        }
        if (searchDTO.durationLonger != null) {
            queryParts.add(durationLonger(SemanticKeys.STARTDATE, SemanticKeys.ENDDATE, searchDTO.durationLonger!!))
        }
        for (flag in (searchDTO.flags ?: emptyList()).filter { !it.isNullOrBlank() }) {
            queryParts.add(equals(flag!!, "true"))
        }
        return and(queryParts)
    }

    fun filters(): Filters {
        val filters = search.getFilters()
        return Filters(
            EventCategory.entries
                .map { Pair(it.name, frontEndName(it)) }
                .sortedWith(Comparator.comparing({ it.second }, String.CASE_INSENSITIVE_ORDER)),
            filters.locationNames.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Pair(it, it) },
            filters.locationCities.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Pair(it, it) },
        )
    }

    private fun mapEvents(result: SearchResultDTO): List<Map<String, String?>> {
        return result.result.map { mapEvent(it) }
    }

    private fun mapEvent(event: Event): Map<String, String?> {
        return mapOf(
            "name" to event.name,
            "description" to event.data[SemanticKeys.DESCRIPTION],
            "url" to event.data[SemanticKeys.URL],
            "startDate" to formatDate(event.startDate),
            "locationName" to (event.data[SemanticKeys.LOCATION_NAME] ?: ""),
            "city" to event.data[SemanticKeys.LOCATION_CITY],
            "category" to mapCategory(event.data[SemanticKeys.CATEGORY]),
            "pictureUrl" to URLEncoder.encode(event.data["pictureUrl"] ?: "", Charsets.UTF_8),
            "accessibleEntry" to event.data[SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY],
            "accessibleSeats" to event.data[SemanticKeys.ACCESSIBILITY_ACCESSIBLESEATS],
            "accessibleToilets" to event.data[SemanticKeys.ACCESSIBILITY_ACCESSIBLETOILETS],
        )
    }

    private fun mapCategory(categoryString: String?): String? {
        if (categoryString != null) {
            val category = try {
                EventCategory.valueOf(categoryString)
            } catch (e: IllegalArgumentException) {
                null
            }
            if (category != null) {
                return when (category) {
                    EventCategory.MUSIC -> "music"
                    EventCategory.ART -> "miscArt"
                    EventCategory.TECH -> "tech"
                    EventCategory.ALL -> "???"
                    EventCategory.OTHER -> null
                }
            }
        }
        return null
    }

    private fun formatDate(startDate: OffsetDateTime): String {
        return formatter.format(startDate.atZoneSameInstant(ZoneId.of("Europe/Vienna")))
    }

    private fun createSearchApi(): Search {
        return Search(searchUrl)
    }

    private fun frontEndName(category: EventCategory): String {
        return when (category) {
            EventCategory.MUSIC -> "Musik"
            EventCategory.ART -> "Kunst"
            EventCategory.TECH -> "Technologie"
            EventCategory.ALL -> "Alle"
            EventCategory.OTHER -> "Andere"
            else -> "???"
        }
    }

    private fun setDefaults(searchDTO: SearchDTO) {
        if (searchDTO.fromDate.isNullOrBlank()) {
            searchDTO.fromDate = LocalDate.now().format(localDateFormatter)
        }
        if (searchDTO.durationShorter == null) {
            searchDTO.durationShorter = DEFAULT_DURATION_SHORTER_VALUE.toDouble()
        }
    }

    data class Filters(
        val categories: List<Pair<String, String>>,
        val locationNames: List<Pair<String, String>>,
        val locationCities: List<Pair<String, String>>,
    )
}

