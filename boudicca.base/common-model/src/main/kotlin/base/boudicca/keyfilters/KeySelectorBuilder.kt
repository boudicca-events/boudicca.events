package base.boudicca.keyfilters

import base.boudicca.model.structured.Key
import base.boudicca.model.structured.KeyFilter
import base.boudicca.model.structured.Variant

class KeySelectorBuilder(
    private val propertyName: String,
) {
    private val variants = mutableListOf<Pair<String, List<String>>>()

    fun thenVariant(
        variantName: String,
        vararg variantValues: String,
    ): KeySelectorBuilder = thenVariant(variantName, variantValues.toList())

    fun thenVariant(
        variantName: String,
        variantValues: List<String>,
    ): KeySelectorBuilder {
        variants.add(Pair(variantName, variantValues))
        return this
    }

    fun thenVariant(variant: Variant): KeySelectorBuilder {
        variants.add(Pair(variant.variantName, listOf(variant.variantValue)))
        return this
    }

    fun build(): KeySelector = KeySelector(propertyName, variants.toList())
}

fun keySelector(
    propertyName: String,
    init: KeySelectorBuilder.() -> Unit = {},
): KeySelector {
    val builder = KeySelector.builder(propertyName)
    builder.init()
    return builder.build()
}

fun keySelector(
    key: Key,
    init: KeySelectorBuilder.() -> Unit = {},
): KeySelector {
    val builder = KeySelector.builder(key)
    builder.init()
    return builder.build()
}

fun keySelector(
    keyFilter: KeyFilter,
    init: KeySelectorBuilder.() -> Unit = {},
): KeySelector {
    val builder = KeySelector.builder(keyFilter)
    builder.init()
    return builder.build()
}
