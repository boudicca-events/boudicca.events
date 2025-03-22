package base.boudicca.keyfilters

import base.boudicca.model.structured.Key
import base.boudicca.model.structured.KeyFilter
import base.boudicca.model.structured.Variant
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@OptIn(ExperimentalJsExport::class)
@JsExport
class KeySelectorBuilder(private val propertyName: String) {
    private val variants = mutableListOf<Pair<String, List<String>>>()

    @JsName("thenVariantWithVarargValues")
    fun thenVariant(variantName: String, vararg variantValues: String): KeySelectorBuilder {
        return thenVariant(variantName, variantValues.toList())
    }

    @JsName("thenVariantWithListValues")
    fun thenVariant(variantName: String, variantValues: List<String>): KeySelectorBuilder {
        variants.add(Pair(variantName, variantValues))
        return this
    }

    @JsName("thenVariantWithVariant")
    fun thenVariant(variant: Variant): KeySelectorBuilder {
        variants.add(Pair(variant.variantName, listOf(variant.variantValue)))
        return this
    }

    fun build(): KeySelector {
        return KeySelector(propertyName, variants.toList())
    }
}

fun keySelector(propertyName: String, init: KeySelectorBuilder.() -> Unit = {}): KeySelector {
    val builder = KeySelector.builder(propertyName)
    builder.init()
    return builder.build()
}

fun keySelector(key: Key, init: KeySelectorBuilder.() -> Unit = {}): KeySelector {
    val builder = KeySelector.builder(key)
    builder.init()
    return builder.build()
}

fun keySelector(keyFilter: KeyFilter, init: KeySelectorBuilder.() -> Unit = {}): KeySelector {
    val builder = KeySelector.builder(keyFilter)
    builder.init()
    return builder.build()
}
