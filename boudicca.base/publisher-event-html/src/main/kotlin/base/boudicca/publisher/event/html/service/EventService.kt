package base.boudicca.publisher.event.html.service

import base.boudicca.SemanticKeys
import base.boudicca.api.search.*
import base.boudicca.api.search.BoudiccaQueryBuilder.after
import base.boudicca.api.search.BoudiccaQueryBuilder.and
import base.boudicca.api.search.BoudiccaQueryBuilder.before
import base.boudicca.api.search.BoudiccaQueryBuilder.contains
import base.boudicca.api.search.BoudiccaQueryBuilder.durationLonger
import base.boudicca.api.search.BoudiccaQueryBuilder.durationShorter
import base.boudicca.api.search.BoudiccaQueryBuilder.equals
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import base.boudicca.publisher.event.html.model.SearchDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
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
    private val searchClient: SearchClient = createSearchClient()
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'", Locale.GERMAN)
    private val localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @Throws(EventServiceException::class)
    fun search(searchDTO: SearchDTO): List<Map<String, String?>> {
        val events = searchClient.queryEvents(QueryDTO(generateQuery(searchDTO), searchDTO.offset ?: 0))
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
        if (!searchDTO.bandName.isNullOrBlank()) {
            queryParts.add(contains(SemanticKeys.CONCERT_BANDLIST, searchDTO.bandName!!))
        }
        return and(queryParts)
    }

    fun filters(): Filters {
        val filters = searchClient.getFiltersFor(
            FilterQueryDTO(
                listOf(
                    FilterQueryEntryDTO(SemanticKeys.LOCATION_NAME),
                    FilterQueryEntryDTO(SemanticKeys.LOCATION_CITY),
                    FilterQueryEntryDTO(SemanticKeys.CONCERT_BANDLIST, true),
                )
            )
        )
        return Filters(
            EventCategory.entries
                .map { Pair(it.name, frontEndName(it)) }
                .sortedWith(Comparator.comparing({ it.second }, String.CASE_INSENSITIVE_ORDER)),
            filters[SemanticKeys.LOCATION_NAME]!!.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Pair(it, it) },
            filters[SemanticKeys.LOCATION_CITY]!!.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Pair(it, it) },
            filters[SemanticKeys.CONCERT_BANDLIST]!!.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Pair(it, it) },
        )
    }

    fun getSources(): List<String> {
        val allSources =
            searchClient.getFiltersFor(FilterQueryDTO(listOf(FilterQueryEntryDTO(SemanticKeys.SOURCES, true))))
        return allSources[SemanticKeys.SOURCES]!!
            .map { normalize(it) }
            .distinct()
            .sortedBy { it }
    }

    private fun mapEvents(result: SearchResultDTO): List<Map<String, String?>> {
        if (result.error != null) {
            throw EventServiceException("error executing query search: " + result.error, null, true)
        }
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
            "accessibilityProperties" to getAllAccessibilityValues(event).joinToString(", "),
        )
    }

    private fun getAllAccessibilityValues(event: Event): List<String> {
        val list = mutableListOf<String>()
        for (property in event.data) {
            if (property.key.startsWith("accessibility.") && property.value.equals("true", true)) {
                list.add(
                    when (property.key) {
                        SemanticKeys.ACCESSIBILITY_ACCESSIBLETOILETS -> "Barrierefreie Toiletten"
                        SemanticKeys.ACCESSIBILITY_ACCESSIBLESEATS -> "Rollstuhlplatz"
                        SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY -> "Barrierefreier Zugang"
                        SemanticKeys.ACCESSIBILITY_AKTIVPASSLINZ -> "Aktivpass möglich"
                        SemanticKeys.ACCESSIBILITY_KULTURPASS -> "Kulturpass möglich"
                        else -> property.key
                    }
                )
            }
        }
        return list.sortedWith(String.CASE_INSENSITIVE_ORDER)
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

    private fun createSearchClient(): SearchClient {
        return SearchClient(searchUrl)
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

    private fun normalize(value: String): String {
        return if (value.startsWith("http")) {
            //treat as url
            try {
                URI.create(value).normalize().host
            } catch (e: IllegalArgumentException) {
                //hm, no url?
                value
            }
        } else {
            value
        }
    }

    data class Filters(
        val categories: List<Pair<String, String>>,
        val locationNames: List<Pair<String, String>>,
        val locationCities: List<Pair<String, String>>,
        val bandNames: List<Pair<String, String>>,
    )
}

