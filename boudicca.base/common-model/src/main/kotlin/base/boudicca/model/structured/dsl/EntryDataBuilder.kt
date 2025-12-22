package base.boudicca.model.structured.dsl

import base.boudicca.format.AbstractFormatAdapter
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.Variant
import base.boudicca.model.structured.VariantConstants

class EntryDataBuilder<T>(
    private val name: String,
    private var defaultFormatAdapter: AbstractFormatAdapter<T>? = null,
) {
    // all combinations of the form
    // description
    // description:lang=de
    // description:lang=de:format=markdown
    // etc.
    private val variantCombinations = mutableMapOf<List<Variant>, String>()

    fun format(formatAdapter: AbstractFormatAdapter<T>) {
        this.defaultFormatAdapter = formatAdapter
    }

    fun data(data: T) {
        val formatAdapterValue = defaultFormatAdapter
        requireNotNull(formatAdapterValue) { "data can only be used when format adapter is set on parent level" }
        variant(formatAdapterValue, data = data)
    }

    fun variant(
        vararg variants: Variant,
        data: T,
    ) {
        val formatAdapterValue = defaultFormatAdapter
        requireNotNull(formatAdapterValue) {
            "variant without format can only be used when format adapter is set on parent level"
        }
        variant(formatAdapterValue, variants = variants, data = data)
    }

    fun <U> variant(
        formatAdapter: AbstractFormatAdapter<U>,
        vararg variants: Variant,
        data: U,
    ) {
        val variantList = variants.toMutableList()
        variantList.addFormatVariant(formatAdapter)

        variantCombinations[variantList.toList()] = formatAdapter.convertToString(data)
    }

    fun build(): Map<Key, String> =
        variantCombinations
            .map { entry ->
                Key(name, entry.key) to entry.value
            }.toMap()
}

private fun <U> MutableList<Variant>.addFormatVariant(formatAdapter: AbstractFormatAdapter<U>) {
    val formatVariant = formatAdapter.variant
    if (!this.any { it.variantName == VariantConstants.FORMAT_VARIANT_NAME } && formatVariant.variantValue.isNotBlank()) {
        this.add(formatVariant)
    }
}
