package events.boudicca.search

import events.boudicca.EventCategory
import events.boudicca.SemanticKeys
import events.boudicca.search.model.*
import events.boudicca.search.util.Utils
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject

private const val SEARCH_TYPE_ALL = "ALL"
private const val SEARCH_TYPE_OTHER = "OTHER"

@ApplicationScoped
class SearchService @Inject constructor(
    private val queryService: QueryService,
) {

    @Volatile
    private var events = emptyList<Event>()

    @Volatile
    private var locationNames = emptySet<String>()

    @Volatile
    private var locationCities = emptySet<String>()

    fun search(searchDTO: SearchDTO): SearchResultDTO {
        val query = createQuery(searchDTO)
        if (query.isNotBlank()) {
            return queryService.query(QueryDTO(query, searchDTO.offset))
        } else {
            return Utils.offset(events, searchDTO.offset)
        }
    }

    fun onEventsUpdate(@Observes events: EventsUpdatedEvent) {
        this.events = Utils.order(events.events)
        locationNames = this.events
            .mapNotNull { it.data?.get(SemanticKeys.LOCATION_NAME) }
            .filter { it.isNotBlank() }
            .toSet()
        locationCities = this.events
            .mapNotNull { it.data?.get(SemanticKeys.LOCATION_CITY) }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun createQuery(searchDTO: SearchDTO): String {
        val queryParts = mutableListOf<String>()
        if (!searchDTO.name.isNullOrBlank()) {
            queryParts.add("\"*\" contains " + escape(searchDTO.name))
        }
        if (!searchDTO.category.isNullOrBlank() && searchDTO.category != SEARCH_TYPE_ALL) {
            queryParts.add("is " + escape(searchDTO.category))
        }
        if (!searchDTO.locationCity.isNullOrBlank()) {
            queryParts.add(SemanticKeys.LOCATION_CITY + " equals " + escape(searchDTO.locationCity))
        }
        if (!searchDTO.locationName.isNullOrBlank()) {
            queryParts.add(SemanticKeys.LOCATION_NAME + " equals " + escape(searchDTO.locationName))
        }
        if (searchDTO.fromDate != null) {
            queryParts.add("after " + formatDate(searchDTO.fromDate))
        }
        if (searchDTO.toDate != null) {
            queryParts.add("before " + formatDate(searchDTO.toDate))
        }
        return queryParts.joinToString(" and ")
    }

    private fun formatDate(date: OffsetDateTime): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun escape(name: String): String {
        return "\"" + name.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }

    fun filters(): Filters {
        return Filters(
            getCategories(),
            getLocationNames(),
            getLocationCities(),
        )
    }

    private fun getCategories(): Set<String> {
        val categories = EventCategory.values().map { it.name }.toMutableSet()
        categories.add(SEARCH_TYPE_ALL)
        categories.add(SEARCH_TYPE_OTHER)
        return categories
    }

    private fun getLocationNames(): Set<String> {
        return locationNames
    }

    private fun getLocationCities(): Set<String> {
        return locationCities
    }

}
