package events.boudicca

import events.boudicca.model.ComplexSearchDto
import events.boudicca.model.Event
import events.boudicca.model.EventKey
import events.boudicca.model.SearchDTO
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class EventService {

    private val events = ConcurrentHashMap<EventKey, Event>()

    fun list(): Set<Event> {
        return events.values.toSet()
    }

    fun add(event: Event) {
        val eventKey = EventKey(event.name, event.startDate)
        val duplicate = events[eventKey]
        //some cheap logging for finding duplicate events between different collectors
        if (duplicate != null && duplicate.data?.get(SemanticKeys.COLLECTORNAME) != event.data?.get(SemanticKeys.COLLECTORNAME)) {
            println("event $event will overwrite $duplicate")
        }

        events[eventKey] = event
    }

    fun search(searchDTO: SearchDTO): Set<Event> {
        val fromDate = searchDTO.fromDate?.toZonedDateTime()
        val toDate = searchDTO.toDate?.toZonedDateTime()
        return events.values
            .filter { e -> fromDate == null || !e.startDate.isBefore(fromDate) }
            .filter { e -> toDate == null || !e.startDate.isAfter(toDate) }
            .filter { e ->
                val data = e.data
                searchDTO.name == null || e.name.lowercase().contains(searchDTO.name.lowercase())
                        || (data != null && data.values.any {
                    it.lowercase().contains(searchDTO.name.lowercase())
                })
            }
            .toSet()
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun searchBy(searchDto: ComplexSearchDto): Set<Event> {
        val filters = mutableSetOf<(Event) -> Boolean>()
        if (!searchDto.anyKeyExactMatch.isNullOrEmpty())
            filters.add { event -> searchDto.anyKeyExactMatch.any { key -> event.data?.containsKey(key) ?: false } }
        if (!searchDto.allKeyExactMatch.isNullOrEmpty())
            filters.add { event -> searchDto.allKeyExactMatch.all { key -> event.data?.containsKey(key) ?: false } }
        if (!searchDto.anyKeyOrValueContains.isNullOrEmpty())
            filters.add { event ->
                searchDto.anyKeyOrValueContains.any {
                    event.data?.keys?.any { key -> key.contains(it.lowercase()) } ?: false ||
                            event.data?.values?.any { value -> value.contains(it.lowercase()) } ?: false
                }
            }
        if (!searchDto.allKeyOrValueContains.isNullOrEmpty())
            filters.add { event ->
                searchDto.allKeyOrValueContains.all {
                    event.data?.keys?.any { key -> key.contains(it.lowercase()) } ?: false ||
                            event.data?.values?.any { value -> value.contains(it.lowercase()) } ?: false
                }
            }
        if (!searchDto.anyKeyOrValueExactMatch.isNullOrEmpty())
            filters.add { event ->
                searchDto.anyKeyOrValueExactMatch.any {
                    event.data?.containsKey(it.lowercase()) ?: false || event.data?.containsValue(it.lowercase()) ?: false
                }
            }
        if (!searchDto.allKeyOrValueExactMatch.isNullOrEmpty())
            filters.add { event ->
                searchDto.allKeyOrValueExactMatch.all {
                    event.data?.containsKey(it.lowercase()) ?: false || event.data?.containsValue(it.lowercase()) ?: false
                }
            }
        if (!searchDto.anyValueForKeyContains.isNullOrEmpty())
            filters.add { event ->
                searchDto.anyValueForKeyContains.any { (key, value) ->
                    event.data?.get(key)?.lowercase()?.contains(value) ?: false
                }
            }
        if (!searchDto.allValueForKeyContains.isNullOrEmpty())
            filters.add { event ->
                searchDto.allValueForKeyContains.all { (key, value) ->
                    event.data?.get(key)?.lowercase()?.contains(value) ?: false
                }
            }
        if (!searchDto.anyValueForKeyExactMatch.isNullOrEmpty())
            filters.add { event ->
                searchDto.anyValueForKeyExactMatch.any { (key, value) ->
                    event.data?.get(key)?.lowercase()?.equals(value) ?: false
                }
            }
        if (!searchDto.allValueForKeyExactMatch.isNullOrEmpty())
            filters.add { event ->
                searchDto.allValueForKeyExactMatch.all { (key, value) ->
                    event.data?.get(key)?.lowercase()?.equals(value) ?: false
                }
            }

        return filters.fold(list()) { events, currentFilter -> events.filter(currentFilter).toSet() }
    }
}
