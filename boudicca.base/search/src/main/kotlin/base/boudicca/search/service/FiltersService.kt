package base.boudicca.search.service

import base.boudicca.api.search.model.FilterQueryDTO
import base.boudicca.api.search.model.FilterQueryEntryDTO
import base.boudicca.api.search.model.FilterResultDTO
import base.boudicca.model.Entry
import base.boudicca.search.service.util.Utils
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class FiltersService {

    @Volatile
    private var entries = emptyList<Entry>()
    private val cache = ConcurrentHashMap<String, List<String>>()

    @EventListener
    fun onEventsUpdate(event: EntriesUpdatedEvent) {
        this.entries = Utils.order(event.entries)
        this.cache.clear()
    }

    fun filtersFor(filterQueryDTO: FilterQueryDTO): FilterResultDTO {
        val result = mutableMapOf<String, List<String>>()

        for (entry in filterQueryDTO.entries) {
            var cacheEntry = cache[entry.name]
            if (cacheEntry == null) {
                cacheEntry = getFilterValuesFor(entry)
                cache[entry.name] = cacheEntry
            }
            result[entry.name] = cacheEntry
        }

        return result
    }

    private fun getFilterValuesFor(entry: FilterQueryEntryDTO): List<String> {
        val result = mutableSetOf<String>()

        for (e in entries) {
            if (e.containsKey(entry.name)) {
                val value = e[entry.name]!!
                if (entry.multiline) {
                    for (line in value.split("\n")) {
                        result.add(line)
                    }
                } else {
                    result.add(value)
                }
            }
        }

        return result.toList()
    }

}