package base.boudicca.keyfilters

import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.Variant


object KeyFilters {

    fun filterKeys(keyFilter: Key, event: StructuredEvent): List<Pair<Key, String>> {
        return filterKeys(keyFilter, event.toEntry())
    }

    fun filterKeys(keyFilter: Key, data: StructuredEntry): List<Pair<Key, String>> {
        return data
            .filter { doesKeyMatchFilter(it.key, keyFilter) }
            .toList()
            .sortedBy { it.first }
    }

    fun doesKeyMatchFilter(
        key: Key,
        keyFilter: Key
    ): Boolean {
        if (keyFilter.name != "*") {
            if (keyFilter.name != key.name) {
                return false
            }
        }
        for (variant in keyFilter.variants) {
            if (!containsVariant(key, variant)) {
                return false
            }
        }
        return true
    }

    fun containsVariant(key: Key, variant: Variant): Boolean {
        if (variant.variantValue == "*") {
            return doesContainVariantName(key, variant.variantName)
        }
        if (variant.variantValue == "") {
            return !doesContainVariantName(key, variant.variantName)
        }
        for (selfVariant in key.variants) {
            if (variant.variantName == selfVariant.variantName) {
                if (variant.variantValue == selfVariant.variantValue) {
                    return true
                }
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