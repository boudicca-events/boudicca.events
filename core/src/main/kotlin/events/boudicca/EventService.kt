package events.boudicca

import events.boudicca.model.*
import io.quarkus.scheduler.Scheduled
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class EventService {

    private val events = ConcurrentHashMap<EventKey, Pair<Event, InternalEventProperties>>()
    private val lastSeenCollectors = ConcurrentHashMap<String, Long>()

    fun list(): Set<Event> {
        return events.values.map { it.first }.toSet()
    }

    fun add(event: Event) {
        val eventKey = EventKey(event)
        val duplicate = events[eventKey]
        //some cheap logging for finding duplicate events between different collectors
        if (duplicate != null && duplicate.first.data?.get(SemanticKeys.COLLECTORNAME) != event.data?.get(SemanticKeys.COLLECTORNAME)) {
            println("event $event will overwrite $duplicate")
        }

        events[eventKey] = Pair(event, InternalEventProperties(System.currentTimeMillis()))
        if (event.data?.containsKey(SemanticKeys.COLLECTORNAME) == true) {
            lastSeenCollectors[event.data[SemanticKeys.COLLECTORNAME]!!] = System.currentTimeMillis()
        }
    }

    fun search(searchDTO: SearchDTO): Set<Event> {
        val fromDate = searchDTO.fromDate?.toZonedDateTime()
        val toDate = searchDTO.toDate?.toZonedDateTime()
        return list()
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

    private val MAX_AGE = Duration.ofDays(3).toMillis()

    @Scheduled(every = "P1D")
    fun cleanup() {
        val toRemoveEvents = events.values
            .filter {
                if (it.first.data?.containsKey(SemanticKeys.COLLECTORNAME) == true) {
                    val collectorName = it.first.data!![SemanticKeys.COLLECTORNAME]!!
                    it.second.timeAdded + MAX_AGE < (lastSeenCollectors[collectorName] ?: 0)
                } else {
                    false
                }
            }

        toRemoveEvents.forEach {
            println("removing event because it got too old: $it")
            events.remove(EventKey(it.first))
        }
    }
}
