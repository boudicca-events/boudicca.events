package base.boudicca.model.structured

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@OptIn(ExperimentalJsExport::class)
@JsExport
abstract class AbstractKeyBuilder<T>(private val name: String) {
    private val variants = mutableListOf<Variant>()

    fun withVariant(variantName: String, variantValue: String): AbstractKeyBuilder<T> {
        return withVariant(Variant(variantName, variantValue))
    }

    @JsName("withVariantObject")
    fun withVariant(variant: Variant): AbstractKeyBuilder<T> {
        variants.add(variant)
        return this
    }

    fun withVariants(newVariants: List<Variant>): AbstractKeyBuilder<T> {
        variants.addAll(newVariants)
        return this
    }

    fun build(): T {
        return build(name, variants)
    }

    @JsName("doBuild")
    abstract fun build(name: String, variants: List<Variant>): T
}
