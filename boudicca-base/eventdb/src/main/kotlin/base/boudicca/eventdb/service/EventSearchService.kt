package base.boudicca.eventdb.service

import base.boudicca.model.Event
import base.boudicca.model.search.ComplexSearchDto
import base.boudicca.model.search.SearchDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
@Deprecated("this search is superseded by the search service")
class EventSearchService @Autowired constructor(private val entryService: EntryService) {

    fun search(searchDTO: SearchDTO): Set<Event> {
        val fromDate = searchDTO.fromDate
        val toDate = searchDTO.toDate
        return entryService.all().asSequence().mapNotNull { Event.fromEntry(it) }
            .filter { e -> fromDate == null || !e.startDate.isBefore(fromDate) }
            .filter { e -> toDate == null || !e.startDate.isAfter(toDate) }
            .filter { e ->
                val data = e.data
                val name = searchDTO.name
                name == null || e.name.lowercase().contains(name.lowercase())
                        || (data.values.any { it.lowercase().contains(name.lowercase()) })
            }
            .toSet()
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun searchBy(searchDto: ComplexSearchDto): Set<Event> {
        val filters = mutableSetOf<(Event) -> Boolean>()
        val anyKeyExactMatch = searchDto.anyKeyExactMatch
        if (!anyKeyExactMatch.isNullOrEmpty())
            filters.add { event -> anyKeyExactMatch.any { key -> event.data.containsKey(key) } }
        val allKeyExactMatch = searchDto.allKeyExactMatch
        if (!allKeyExactMatch.isNullOrEmpty())
            filters.add { event -> allKeyExactMatch.all { key -> event.data.containsKey(key) } }
        val anyKeyOrValueContains = searchDto.anyKeyOrValueContains
        if (!anyKeyOrValueContains.isNullOrEmpty())
            filters.add { event ->
                anyKeyOrValueContains.any {
                    event.data.keys.any { key -> key.contains(it.lowercase()) } ||
                            event.data.values.any { value -> value.contains(it.lowercase()) }
                }
            }
        val allKeyOrValueContains = searchDto.allKeyOrValueContains
        if (!allKeyOrValueContains.isNullOrEmpty())
            filters.add { event ->
                allKeyOrValueContains.all {
                    event.data.keys.any { key -> key.contains(it.lowercase()) } ||
                            event.data.values.any { value -> value.contains(it.lowercase()) }
                }
            }
        val anyKeyOrValueExactMatch = searchDto.anyKeyOrValueExactMatch
        if (!anyKeyOrValueExactMatch.isNullOrEmpty())
            filters.add { event ->
                anyKeyOrValueExactMatch.any {
                    event.data.containsKey(it.lowercase()) || event.data.containsValue(it.lowercase())
                }
            }
        val allKeyOrValueExactMatch = searchDto.allKeyOrValueExactMatch
        if (!allKeyOrValueExactMatch.isNullOrEmpty())
            filters.add { event ->
                allKeyOrValueExactMatch.all {
                    event.data.containsKey(it.lowercase()) || event.data.containsValue(it.lowercase())
                }
            }
        val anyValueForKeyContains = searchDto.anyValueForKeyContains
        if (!anyValueForKeyContains.isNullOrEmpty())
            filters.add { event ->
                anyValueForKeyContains.any { (key, value) ->
                    event.data[key]?.lowercase()?.contains(value) ?: false
                }
            }
        val allValueForKeyContains = searchDto.allValueForKeyContains
        if (!allValueForKeyContains.isNullOrEmpty())
            filters.add { event ->
                allValueForKeyContains.all { (key, value) ->
                    event.data[key]?.lowercase()?.contains(value) ?: false
                }
            }
        val anyValueForKeyExactMatch = searchDto.anyValueForKeyExactMatch
        if (!anyValueForKeyExactMatch.isNullOrEmpty())
            filters.add { event ->
                anyValueForKeyExactMatch.any { (key, value) ->
                    event.data[key]?.lowercase()?.equals(value) ?: false
                }
            }
        val allValueForKeyExactMatch = searchDto.allValueForKeyExactMatch
        if (!allValueForKeyExactMatch.isNullOrEmpty())
            filters.add { event ->
                allValueForKeyExactMatch.all { (key, value) ->
                    event.data[key]?.lowercase()?.equals(value) ?: false
                }
            }

        return filters
            .fold(
                entryService.all().mapNotNull { Event.fromEntry(it) }.toSet()
            )
            { events, currentFilter ->
                events.filter(currentFilter).toSet()
            }
            .toSet()
    }
}
