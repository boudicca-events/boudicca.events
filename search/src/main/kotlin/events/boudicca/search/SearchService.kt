package events.boudicca.search

import events.boudicca.EventCategory
import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import events.boudicca.search.model.Filters
import events.boudicca.search.model.SearchDTO
import events.boudicca.search.util.Utils
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

private const val SEARCH_TYPE_ALL = "ALL"
private const val SEARCH_TYPE_OTHER = "OTHER"

@ApplicationScoped
class SearchService @Inject constructor(
    private val synchronizationService: SynchronizationService
) {

    fun search(searchDTO: SearchDTO): List<Event> {
        return Utils.offset(Utils.order(filter(searchDTO)), searchDTO.offset)
    }

    fun filters(): Filters {
        return Filters(
            getCategories(),
            getLocationNames(),
            getLocationCities(),
        )
    }

    fun filter(searchDTO: SearchDTO): Collection<Event> {
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


    private fun matchesFilter(event: Event, searchDTO: SearchDTO): Boolean {
        if (!searchDTO.category.isNullOrBlank()) {
            if (!matchCategoryFilter(searchDTO.category, event)) {
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

    private fun matchCategoryFilter(category: String, event: Event): Boolean {
        if (category == SEARCH_TYPE_ALL) {
            return true
        }

        val type = event.data?.get(SemanticKeys.TYPE)
        val eventCategory = if (type != null) {
            EventCategory.getForType(type)
        } else {
            null
        }
        if (eventCategory == null) {
            return category == SEARCH_TYPE_OTHER
        }
        return eventCategory.name == category
    }


}
