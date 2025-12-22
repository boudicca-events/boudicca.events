package base.boudicca.model.structured

/**
 * represents a parsed Key of a Key-Value pair which consists of the name and all the variants (which are sorted canonically)
 */
class Key(
    name: String,
    variants: List<Variant> = emptyList(),
) : AbstractKey<Key>(name, variants) {
    override fun validate() {
        require(name != "*") { "key name is not allowed to be '*'" }
        variants.forEach {
            require(it.variantValue != "*") { "for a key a variant value is not allowed to be '*'" }
            require(it.variantValue != "") { "for a key a variant value is not allowed to be the empty string" }
        }
    }

    companion object {
        fun parse(keyFilter: String): Key = KeyUtils.parseKey(keyFilter)

        fun builder(propertyName: String): KeyBuilder = KeyBuilder(propertyName)
    }

    fun asKeyFilter(): KeyFilter = KeyFilter(name, variants)

    override fun toBuilder() = KeyBuilder(name).withVariants(variants)
}
