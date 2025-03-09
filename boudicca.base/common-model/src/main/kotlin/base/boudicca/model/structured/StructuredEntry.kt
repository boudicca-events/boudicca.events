package base.boudicca.model.structured

import base.boudicca.Property
import base.boudicca.keyfilters.KeyFilters
import base.boudicca.keyfilters.KeySelector
import base.boudicca.model.Entry
import base.boudicca.model.structured.dsl.StructuredEntryBuilder
import java.util.*

/**
 * as with the [Entry] class, a StructuredEntry is simple a typealias for a Map<Key, String>
 */
typealias StructuredEntry = Map<Key, String>

fun StructuredEntry.toFlatEntry(): Entry {
    return KeyUtils.toFlatKeyValuePairs(this)
}

fun StructuredEntry.toEvent(): Optional<StructuredEvent> {
    return StructuredEvent.fromEntry(this)
}

fun StructuredEntry.toBuilder(): StructuredEntryBuilder {
    return StructuredEntryBuilder(this)
}

/**
 * get property values from this entry. please note that if a property value cannot be parsed it will silently ignore this value
 */
fun <T> StructuredEntry.getProperty(property: Property<T>): List<Pair<Key, T>> {
    return getProperty(property, null)
}

/**
 * get property values with a specific language from this entry. please note that if a property value cannot be parsed it will silently ignore this value
 */
fun <T> StructuredEntry.getProperty(property: Property<T>, language: String?): List<Pair<Key, T>> {
    return KeyFilters.filterKeys(property.getKeyFilter(language), this).mapNotNull {
        try {
            val parsedValue = property.parseFromString(it.second)
            Pair(it.first, parsedValue)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

fun StructuredEntry.filterKeys(keyFilter: KeyFilter): List<Pair<Key, String>> {
    return KeyFilters.filterKeys(keyFilter, this)
}

fun StructuredEntry.selectKey(keySelector: KeySelector): Optional<Pair<Key, String>> {
    return keySelector.selectSingle(this)
}

