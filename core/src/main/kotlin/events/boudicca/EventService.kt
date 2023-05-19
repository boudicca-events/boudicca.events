package events.boudicca

import events.boudicca.model.ComplexSearchDto
import events.boudicca.model.Event
import events.boudicca.model.SearchDTO
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class EventService {

    private val events = mutableSetOf<Event>()

    fun list(): Set<Event> {
        return events
    }

    fun add(event: Event) {
        events.removeIf { eventInDb -> eventInDb.name == event.name }
        events.add(event)
    }

    fun search(searchDTO: SearchDTO): Set<Event> {
        val fromDate = searchDTO.fromDate?.toZonedDateTime()
        val toDate = searchDTO.toDate?.toZonedDateTime()
        return events
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

    init {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val zone = ZoneId.of("Europe/Vienna")
        events.add(
            Event(
                name = "Linz hACkT",
                startDate = ZonedDateTime.of(LocalDateTime.parse("2023-03-24 15:00", formatter), zone),
                data = mapOf("tags" to listOf("techcommunity", "hackaton").toString())
            )
        )
        events.add(
            Event(
                name = "Cloudflight Coding Contest",
                startDate = ZonedDateTime.of(LocalDateTime.parse("2023-03-31 14:00", formatter), zone),
                data = mapOf(
                    "start.location.name" to "JKU Linz",
                    "tags" to listOf("education", "techcommunity").toString()
                )
            )
        )
    }
}
