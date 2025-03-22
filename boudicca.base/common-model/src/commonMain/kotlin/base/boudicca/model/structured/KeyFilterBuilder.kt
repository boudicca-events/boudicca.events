package base.boudicca.model.structured

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class KeyFilterBuilder(name: String) : AbstractKeyBuilder<KeyFilter>(name) {
    override fun build(name: String, variants: List<Variant>): KeyFilter {
        return KeyFilter(name, variants)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun keyFilter(name: String, init: KeyFilterBuilder.() -> Unit = {}): KeyFilter {
    val builder = KeyFilterBuilder(name)
    builder.init()
    return builder.build()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun modifyKeyFilter(keyFilter: KeyFilter, init: KeyFilterBuilder.() -> Unit = {}): KeyFilter {
    val builder = keyFilter.toBuilder() as KeyFilterBuilder
    builder.init()
    return builder.build()
}
