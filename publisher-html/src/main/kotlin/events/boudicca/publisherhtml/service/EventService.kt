package events.boudicca.publisherhtml.service

import events.boudicca.EventCategory
import events.boudicca.SemanticKeys
import events.boudicca.api.search.BoudiccaQueryBuilder.after
import events.boudicca.api.search.BoudiccaQueryBuilder.and
import events.boudicca.api.search.BoudiccaQueryBuilder.before
import events.boudicca.api.search.BoudiccaQueryBuilder.contains
import events.boudicca.api.search.BoudiccaQueryBuilder.durationLonger
import events.boudicca.api.search.BoudiccaQueryBuilder.durationShorter
import events.boudicca.api.search.BoudiccaQueryBuilder.equals
import events.boudicca.publisherhtml.model.SearchDTO
import events.boudicca.search.openapi.ApiClient
import events.boudicca.search.openapi.api.SearchResourceApi
import events.boudicca.search.openapi.model.Event
import events.boudicca.search.openapi.model.QueryDTO
import events.boudicca.search.openapi.model.SearchResultDTO
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
class EventService @Autowired constructor(
    @Value("\${boudicca.search.url}") private val search: String,
    @Value("\${boudicca.search.additionalFilter:}") private val additionalFilter: String,
) {
    private val searchApi: SearchResourceApi = createSearchApi()
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'", Locale.GERMAN)
    private val localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun search(searchDTO: SearchDTO): List<Map<String, String?>> {
        val events = searchApi.queryPost(QueryDTO().query(generateQuery(searchDTO)).offset(searchDTO.offset))
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
        return if (additionalFilter.isNotBlank()) {
            and(query, additionalFilter)
        } else {
            query
        }
    }

    private fun buildQuery(searchDTO: SearchDTO): String {
        val queryParts = mutableListOf<String>()
        if (!searchDTO.name.isNullOrBlank()) {
            queryParts.add(contains("*", searchDTO.name!!))
        }
        if (!searchDTO.category.isNullOrBlank() && searchDTO.category != SEARCH_TYPE_ALL) {
            queryParts.add(equals("category", searchDTO.category!!))
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
        val filters = searchApi.filtersGet()
        return Filters(
            filters.categories.map { Pair(it, frontEndName(it)) }.sortedBy { it.second },
            filters.locationNames.sorted().map { Pair(it, it) },
            filters.locationCities.sorted().map { Pair(it, it) },
        )
    }

    private fun mapEvents(result: SearchResultDTO): List<Map<String, String?>> {
        //TODO @patzi: result contains result.totalResults, do something with it
        return result.result.map { mapEvent(it) }
    }

    private fun mapEvent(event: Event): Map<String, String?> {
        return mapOf(
            "name" to event.name,
            "description" to event.data?.get(SemanticKeys.DESCRIPTION),
            "url" to event.data?.get(SemanticKeys.URL),
            "startDate" to formatDate(event.startDate),
            "locationName" to (event.data?.get(SemanticKeys.LOCATION_NAME) ?: ""),
            "city" to event.data?.get(SemanticKeys.LOCATION_CITY),
            "category" to mapType(event.data?.get(SemanticKeys.TYPE)),
            "pictureUrl" to URLEncoder.encode(event.data?.get("pictureUrl") ?: "", Charsets.UTF_8),
        )
    }

    private fun mapType(type: String?): String? {
        val category = EventCategory.getForType(type)
        if (category != null) {
            return frontEndCategoryName(category)
        }
        return null
    }

    private fun formatDate(startDate: OffsetDateTime): String {
        return formatter.format(startDate.atZoneSameInstant(ZoneId.of("Europe/Vienna")))
    }

    private fun createSearchApi(): SearchResourceApi {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(search)
        return SearchResourceApi(apiClient)
    }

    private fun frontEndCategoryName(type: EventCategory): String {
        return when (type) {
            EventCategory.MUSIC -> "music"
            EventCategory.ART -> "miscArt"
            EventCategory.TECH -> "tech"
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

