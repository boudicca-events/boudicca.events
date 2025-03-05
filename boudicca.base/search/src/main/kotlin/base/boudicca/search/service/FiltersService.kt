package base.boudicca.search.service

import base.boudicca.api.search.model.FilterQueryDTO
import base.boudicca.api.search.model.FilterQueryEntryDTO
import base.boudicca.api.search.model.FilterResultDTO
import base.boudicca.format.ListFormatAdapter
import base.boudicca.keyfilters.KeyFilters
import base.boudicca.model.structured.*
import base.boudicca.model.toStructuredEntry
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class FiltersService {

    @Volatile
    private var entries = emptyList<StructuredEntry>()
    private val cache = ConcurrentHashMap<String, List<String>>()

    @EventListener
    fun onEventsUpdate(event: EntriesUpdatedEvent) {
        this.entries = event.entries.toList().map { it.toStructuredEntry() }
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

    private fun getFilterValuesFor(filterQuery: FilterQueryEntryDTO): List<String> {
        val result = mutableSetOf<String>()

        for (entry in entries) {
            entry
                .filterKeys(Key.parse(filterQuery.name))
                .flatMap {
                    //TODO can we find utils/a generic way to do this check?
                    if (isList(it.first)) {
                        ListFormatAdapter().fromString(it.second)
                    } else {
                        listOf(it.second)
                    }
                }
                .forEach {
                    result.add(it)
                }
        }

        return result.toList()
    }

    //TODO find better place to place those utils?
    private fun isList(key: Key): Boolean {
        return KeyFilters.containsVariant(
            key,
            Variant(
                VariantConstants.FORMAT_VARIANT_NAME,
                VariantConstants.FormatVariantConstants.LIST_FORMAT_NAME
            )
        )
    }

}
