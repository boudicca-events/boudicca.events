package base.boudicca.keyfilters

import base.boudicca.model.Event
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.Variant
import base.boudicca.model.toStructuredEntry
import java.util.*

class KeySelector private constructor(
    private val propertyName: String,
    private val variants: List<Pair<String, List<String>>>
) {
    fun selectSingle(event: StructuredEvent): Optional<Pair<Key, String>> {
        return selectSingle(Event.toEntry(event.toFlatEvent()).toStructuredEntry())
    }

    fun selectSingle(properties: StructuredEntry): Optional<Pair<Key, String>> {
        val variantList = mutableListOf<Variant>()
        val variantIndexes = IntArray(variants.size) { 0 }
        var currentVariant = 0

        while (true) {
            if (currentVariant < 0) {
                //we exhausted all options from all variants
                return Optional.empty()
            }
            if (currentVariant >= variants.size) {
                //selectorList is full
                val keyFilter = Key
                    .builder(propertyName)
                    .withVariants(variantList)
                    .build()
                val keys = KeyFilters.filterKeys(keyFilter, properties)
                if (keys.isNotEmpty()) {
                    return Optional.of(keys.first())
                } else {
                    currentVariant--
                    if (variantList.isNotEmpty()) {
                        variantList.removeLast()
                    }
                }
            } else {
                val currentVariantPlace = variantIndexes[currentVariant]
                if (currentVariantPlace >= variants[currentVariant].second.size) {
                    //we exhausted the current variant, reset and go back up
                    variantIndexes[currentVariant] = 0
                    currentVariant--
                    if (variantList.isNotEmpty()) {
                        variantList.removeLast()
                    }
                } else {
                    //try a new value from the current variant
                    variantList.add(
                        Variant(
                            variants[currentVariant].first,
                            variants[currentVariant].second[currentVariantPlace]
                        )
                    )
                    variantIndexes[currentVariant] = variantIndexes[currentVariant] + 1
                    currentVariant++
                }
            }
        }
    }

    companion object {
        fun builder(propertyName: String): KeySelectorBuilder {
            return KeySelectorBuilder(propertyName)
        }
    }

    class KeySelectorBuilder internal constructor(private val propertyName: String) {
        private val variants = mutableListOf<Pair<String, List<String>>>()

        fun thenVariant(variantName: String, variantValues: List<String>): KeySelectorBuilder {
            variants.add(Pair(variantName, variantValues))
            return this
        }

        fun build(): KeySelector {
            return KeySelector(propertyName, variants.toList())
        }
    }

}
