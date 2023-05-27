package events.boudicca.search

import events.boudicca.EventCategory
import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import events.boudicca.search.model.Filters
import events.boudicca.search.model.QueryDTO
import events.boudicca.search.model.SearchDTO
import events.boudicca.search.util.Utils
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

private const val SEARCH_TYPE_ALL = "ALL"
private const val SEARCH_TYPE_OTHER = "OTHER"

@ApplicationScoped
class SearchService @Inject constructor(
    private val synchronizationService: SynchronizationService,
    private val queryService: QueryService,
) {

    fun search(searchDTO: SearchDTO): List<Event> {
        val query = createQuery(searchDTO)
        if (query.isNotBlank()) {
            return queryService.query(QueryDTO(query, searchDTO.offset))
        } else {
            //TODO what to do about this?
            return Utils.offset(Utils.order(synchronizationService.getEvents()), searchDTO.offset)
        }
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
        return synchronizationService.getEvents()
            .mapNotNull { it.data?.get(SemanticKeys.LOCATION_NAME) }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun getLocationCities(): Set<String> {
        return synchronizationService.getEvents()
            .mapNotNull { it.data?.get(SemanticKeys.LOCATION_CITY) }
            .filter { it.isNotBlank() }
            .toSet()
    }

}
