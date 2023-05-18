package events.boudicca

import events.boudicca.model.Event
import events.boudicca.model.SearchDTO
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class SearchService @Inject constructor(
    private val synchronizationService: SynchronizationService
) {

    fun search(searchDTO: SearchDTO): Set<Event> {
        val fromDate = searchDTO.fromDate?.toZonedDateTime()
        val toDate = searchDTO.toDate?.toZonedDateTime()
        return synchronizationService.getEvents()
            .filter { e -> fromDate == null || !e.startDate.isBefore(fromDate) }
            .filter { e -> toDate == null || !e.startDate.isAfter(toDate) }
            .filter { e ->
                val data = e.data
                searchDTO.name == null || e.name.lowercase().contains(searchDTO.name!!.lowercase())
                        || (data != null && data.values.any {
                    it.lowercase().contains(searchDTO.name!!.lowercase())
                })
            }
            .toSet()
    }

}
