package base.boudicca.keyfilters

import base.boudicca.model.Event
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.KeyFilter
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.Variant
import base.boudicca.model.toStructuredEntry
import java.util.*

/**
 * Extending KeyFilters, this KeySelector helps you when you need to select a single value of all possible variants.
 * This KeySelector class helps you define priorities when selecting variants, so you get the most fitting value to display.
 *
 * One Example would be you wanting to select what variant to show for the "description" property of events. In this example
 * we care about two variants: 1) Language and 2) Format. We prioritize the language, and only then the format. The code could look like this:
 *
 * ```
 *   val selectedValue = KeySelector.builder(propertyName)
 *             .thenVariant(
 *                 VariantConstants.LANGUAGE_VARIANT_NAME,
 *                 listOf(
 *                     getPreferredLanguage(),
 *                     VariantConstants.LanguageVariantConstants.DEFAULT_LANGUAGE_NAME,
 *                     VariantConstants.ANY_VARIANT_SELECTOR
 *                 )
 *             )
 *             .thenVariant(
 *                 VariantConstants.FORMAT_VARIANT_NAME,
 *                 listOf(
 *                     FormatVariantConstants.MARKDOWN_FORMAT_NAME,
 *                     FormatVariantConstants.TEXT_FORMAT_NAME
 *                 )
 *             )
 *             .build()
 *             .selectSingle(event)
 * ```
 *
 * Note that the KeySelector currently does not handle the format variant, you have to convert the value manually.
 */
class KeySelector(
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
                val keyFilter = KeyFilter(propertyName, variantList)
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

        fun builder(key: Key): KeySelectorBuilder {
            val builder = KeySelectorBuilder(key.name)
            key.variants.forEach { builder.thenVariant(it) }
            return builder
        }

        fun builder(keyFilter: KeyFilter): KeySelectorBuilder {
            val builder = KeySelectorBuilder(keyFilter.name)
            keyFilter.variants.forEach { builder.thenVariant(it) }
            return builder
        }
    }

}
