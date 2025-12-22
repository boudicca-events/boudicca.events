package base.boudicca.model.structured

class KeyBuilder(
    name: String,
) : AbstractKeyBuilder<Key>(name) {
    override fun build(
        name: String,
        variants: List<Variant>,
    ): Key = Key(name, variants)
}

fun key(
    name: String,
    init: KeyBuilder.() -> Unit = {},
): Key {
    val builder = KeyBuilder(name)
    builder.init()
    return builder.build()
}

fun modifyKey(
    key: Key,
    init: KeyBuilder.() -> Unit = {},
): Key {
    val builder = key.toBuilder() as KeyBuilder
    builder.init()
    return builder.build()
}
