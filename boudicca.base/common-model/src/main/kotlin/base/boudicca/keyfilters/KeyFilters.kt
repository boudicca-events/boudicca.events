package base.boudicca.keyfilters

import base.boudicca.model.structured.Key
import base.boudicca.model.structured.KeyFilter
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.Variant

/**
 * utility methods for filtering keys of Events/Entries. you can use a Key as a KeyFilter to select only matching keys of an event/entry.
 * this is especially needed to work with variants, because with KeyFilters you can for example select all "descriptions" in all languages, or only certain languages.
 * see DATA_MODEL.md docs for more information on key filters
 */
object KeyFilters {

    fun filterKeys(keyFilter: KeyFilter, event: StructuredEvent): List<Pair<Key, String>> {
        return filterKeys(keyFilter, event.toEntry())
    }

    fun filterKeys(keyFilter: KeyFilter, data: StructuredEntry): List<Pair<Key, String>> {
        return data
            .filter { doesKeyMatchFilter(it.key, keyFilter) }
            .toList()
            .sortedBy { it.first }
    }

    fun doesKeyMatchFilter(
        key: Key,
        keyFilter: KeyFilter
    ): Boolean {
        return ((isWildcard(keyFilter) || keyFilter.name == key.name) &&
                keyContainsAllVariants(keyFilter, key))
    }

    private fun keyContainsAllVariants(keyFilter: KeyFilter, key: Key) =
        keyFilter.variants.all { variant -> containsVariant(key, variant) }

    private fun isWildcard(keyFilter: KeyFilter) = keyFilter.name == "*"

    @Suppress("ReturnCount")
    fun containsVariant(key: Key, variant: Variant): Boolean {
        if (variant.variantValue == "*") {
            return doesContainVariantName(key, variant.variantName)
        }
        if (variant.variantValue == "") {
            return !doesContainVariantName(key, variant.variantName)
        }
        for (selfVariant in key.variants) {
            if (variant.variantName == selfVariant.variantName &&
                variant.variantValue == selfVariant.variantValue
            ) {
                return true

            }
        }
        return false
    }

    private fun doesContainVariantName(key: Key, variantName: String): Boolean {
        for (variant in key.variants) {
            if (variantName == variant.variantName) {
                return true
            }
        }
        return false
    }

    fun doesContainVariantValue(key: Key, variantName: String, variantValues: List<String>): Boolean {
        for (value in variantValues) {
            if (containsVariant(key, Variant(variantName, value))) {
                return true
            }
        }
        return false
    }
}
