package at.cnoize.boudicca

import java.time.Instant

class EventService {

    private val events = mutableSetOf<Event>()

    fun list(): Set<Event> {
        return events
    }

    fun add(event: Event) {
        events.add(event)
    }

    fun search(searchDTO: SearchDTO): Set<Event> {
        return events
            .filter { e -> searchDTO.fromDate == null || !e.startDate.isBefore(searchDTO.fromDate) }
            .filter { e -> searchDTO.toDate == null || !e.startDate.isAfter(searchDTO.toDate) }
            .filter { e -> searchDTO.name == null || e.name.contains(searchDTO.name!!) }
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
                    event.data?.keys?.any { key -> key.contains(it) } ?: false ||
                            event.data?.values?.any { value -> value.contains(it) } ?: false
                }
            }
        if (!searchDto.allKeyOrValueContains.isNullOrEmpty())
            filters.add { event ->
                searchDto.allKeyOrValueContains.all {
                    event.data?.keys?.any { key -> key.contains(it) } ?: false ||
                            event.data?.values?.any { value -> value.contains(it) } ?: false
                }
            }
        if (!searchDto.anyKeyOrValueExactMatch.isNullOrEmpty())
            filters.add { event ->
                searchDto.anyKeyOrValueExactMatch.any {
                    event.data?.containsKey(it) ?: false || event.data?.containsValue(it) ?: false
                }
            }
        if (!searchDto.allKeyOrValueExactMatch.isNullOrEmpty())
            filters.add { event ->
                searchDto.allKeyOrValueExactMatch.all {
                    event.data?.containsKey(it) ?: false || event.data?.containsValue(it) ?: false
                }
            }
        if (!searchDto.anyValueForKeyContains.isNullOrEmpty())
            filters.add { event ->
                searchDto.anyValueForKeyContains.any { (key, value) ->
                    event.data?.get(key)?.contains(value) ?: false
                }
            }
        if (!searchDto.allValueForKeyContains.isNullOrEmpty())
            filters.add { event ->
                searchDto.allValueForKeyContains.all { (key, value) ->
                    event.data?.get(key)?.contains(value) ?: false
                }
            }
        if (!searchDto.anyValueForKeyExactMatch.isNullOrEmpty())
            filters.add { event ->
                searchDto.anyValueForKeyExactMatch.any { (key, value) ->
                    event.data?.get(key)?.equals(value) ?: false
                }
            }
        if (!searchDto.allValueForKeyExactMatch.isNullOrEmpty())
            filters.add { event ->
                searchDto.allValueForKeyExactMatch.all { (key, value) ->
                    event.data?.get(key)?.equals(value) ?: false
                }
            }

        return filters.fold(list()) { events, currentFilter -> events.filter(currentFilter).toSet() }
    }

    init {
        events.add(Event(name = "TestEvent", startDate = Instant.now()))
        events.add(Event(name = "TestEvent2", startDate = Instant.now()))
        events.add(Event(name = "TestEvent3", startDate = Instant.now()))
        events.add(
            Event(
                name = "TestEvent4",
                startDate = Instant.now(),
                data = mapOf("key" to "value", "test" to "testvalue")
            )
        )
    }
}
