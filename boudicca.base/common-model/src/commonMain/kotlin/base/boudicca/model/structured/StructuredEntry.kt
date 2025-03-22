package base.boudicca.model.structured

import base.boudicca.Property
import base.boudicca.keyfilters.KeyFilters
import base.boudicca.keyfilters.KeySelector
import base.boudicca.model.Entry
import base.boudicca.model.structured.dsl.StructuredEntryBuilder
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * as with the [Entry] class, a StructuredEntry is simple a typealias for a Map<Key, String>
 */
typealias StructuredEntry = Map<Key, String>

@OptIn(ExperimentalJsExport::class)
@JsExport
fun StructuredEntry.toFlatEntry(): Entry {
    return KeyUtils.toFlatKeyValuePairs(this)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun StructuredEntry.toEvent(): StructuredEvent? {
    return StructuredEvent.fromEntry(this)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun StructuredEntry.toBuilder(): StructuredEntryBuilder {
    return StructuredEntryBuilder(this)
}

/**
 * get property values from this entry. please note that if a property value cannot be parsed it will silently ignore this value
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun <T> StructuredEntry.getProperty(property: Property<T>): List<Pair<Key, T>> {
    return getProperty(property, null)
}

/**
 * get property values with a specific language from this entry. please note that if a property value cannot be parsed it will silently ignore this value
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("getPropertyWithLanguage")
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

@OptIn(ExperimentalJsExport::class)
@JsExport
fun StructuredEntry.filterKeys(keyFilter: KeyFilter): List<Pair<Key, String>> {
    return KeyFilters.filterKeys(keyFilter, this)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun StructuredEntry.selectKey(keySelector: KeySelector): Pair<Key, String>? {
    return keySelector.selectSingle(this)
}

