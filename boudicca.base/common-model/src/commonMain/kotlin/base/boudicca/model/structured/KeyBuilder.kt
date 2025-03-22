package base.boudicca.model.structured

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class KeyBuilder(name: String) : AbstractKeyBuilder<Key>(name) {
    override fun build(name: String, variants: List<Variant>): Key {
        return Key(name, variants)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun key(name: String, init: KeyBuilder.() -> Unit = {}): Key {
    val builder = KeyBuilder(name)
    builder.init()
    return builder.build()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun modifyKey(key: Key, init: KeyBuilder.() -> Unit = {}): Key {
    val builder = key.toBuilder() as KeyBuilder
    builder.init()
    return builder.build()
}
