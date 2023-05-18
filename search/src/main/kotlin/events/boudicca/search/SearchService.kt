package events.boudicca.search

import events.boudicca.EventTypes
import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import events.boudicca.search.model.Filters
import events.boudicca.search.model.SearchDTO
import java.time.ZonedDateTime
import java.util.function.Function
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

private const val SEARCH_TYPE_ALL = "ALL"
private const val SEARCH_TYPE_OTHER = "OTHER"

@ApplicationScoped
class SearchService @Inject constructor(
    private val synchronizationService: SynchronizationService
) {

    fun search(searchDTO: SearchDTO): List<Event> {
        return order(filter(searchDTO))
    }

    fun filters(): Filters {
        return Filters(
            getTypes(),
            getLocationNames(),
            getLocationCities(),
        )
    }

    private fun getTypes(): Set<String> {
        val types = EventTypes.values().map { it.name }.toMutableSet()
        types.add(SEARCH_TYPE_ALL)
        types.add(SEARCH_TYPE_OTHER)
        return types
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

    private fun order(events: Collection<Event>): List<Event> {
        return events
            .toList()
            .sortedWith(
                Comparator
                    .comparing<Event?, ZonedDateTime?> { it.startDate }
                    .thenComparing(Function { it.name })
            )
    }

    private fun filter(searchDTO: SearchDTO): Collection<Event> {
        val fromDate = searchDTO.fromDate?.toZonedDateTime()
        val toDate = searchDTO.toDate?.toZonedDateTime()
        return synchronizationService.getEvents()
            .filter { e -> fromDate == null || !e.startDate.isBefore(fromDate) }
            .filter { e -> toDate == null || !e.startDate.isAfter(toDate) }
            .filter { e ->
                val data = e.data
                searchDTO.name == null || e.name.lowercase().contains(searchDTO.name.lowercase())
                        || (data != null && data.values.any {
                    it.lowercase().contains(searchDTO.name.lowercase())
                })
            }.filter { matchesFilter(it, searchDTO) }
    }

    private fun matchesFilter(event: Event, searchDTO: SearchDTO): Boolean {
        if (!searchDTO.type.isNullOrBlank()) {
            if (!matchTypeFilter(searchDTO.type, event)) {
                return false
            }
        }
        if (!searchDTO.locationName.isNullOrBlank()
            && event.data?.get(SemanticKeys.LOCATION_NAME) != searchDTO.locationName
        ) {
            return false
        }
        if (!searchDTO.locationCity.isNullOrBlank()
            && event.data?.get(SemanticKeys.LOCATION_CITY) != searchDTO.locationCity
        ) {
            return false
        }
        return true
    }

    private fun matchTypeFilter(type: String, event: Event): Boolean {
        if (type == SEARCH_TYPE_ALL) {
            return true
        }

        val lowerCaseType = event.data?.get(SemanticKeys.TYPE)?.lowercase() ?: ""
        val eventType = EventTypes.values().firstOrNull { it.types.contains(lowerCaseType) }
        if (eventType == null) {
            return type == SEARCH_TYPE_OTHER
        }
        return eventType.name == type
    }

}
