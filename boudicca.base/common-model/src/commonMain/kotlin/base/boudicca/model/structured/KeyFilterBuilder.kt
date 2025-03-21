package base.boudicca.model.structured

class KeyFilterBuilder(name: String) : AbstractKeyBuilder<KeyFilter>(name) {
    override fun build(name: String, variants: List<Variant>): KeyFilter {
        return KeyFilter(name, variants)
    }
}

fun keyFilter(name: String, init: KeyFilterBuilder.() -> Unit = {}): KeyFilter {
    val builder = KeyFilterBuilder(name)
    builder.init()
    return builder.build()
}

fun modifyKeyFilter(keyFilter: KeyFilter, init: KeyFilterBuilder.() -> Unit = {}): KeyFilter {
    val builder = keyFilter.toBuilder() as KeyFilterBuilder
    builder.init()
    return builder.build()
}
