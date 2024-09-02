package base.boudicca.model.structured

data class Key(val name: String, val variants: List<Variant>) : Comparable<Key> {
    fun toKeyString(): String {
        if (variants.isEmpty()) {
            return name
        }
        return name + ":" + variants.joinToString(":") { it.toKeyString() }
    }

    companion object {
        val COMPARATOR = compareBy<Key> { it.toKeyString() } //TODO want better compare here?

        fun parse(keyFilter: String): Key {
            val keyVariantPair = KeyUtils.parseKey(keyFilter)
            return Key(keyVariantPair.first, keyVariantPair.second)
        }

        fun builder(propertyName: String): KeyBuilder {
            return KeyBuilder(propertyName)
        }
    }

    override fun compareTo(other: Key): Int {
        return COMPARATOR.compare(this, other)
    }

    class KeyBuilder internal constructor(private val propertyName: String) {
        private val variants = mutableListOf<Variant>()

        fun withVariant(variantName: String, variantValue: String): KeyBuilder {
            return withVariant(Variant(variantName, variantValue))
        }

        fun withVariant(variant: Variant): KeyBuilder {
            variants.add(variant)
            return this
        }

        fun withVariants(newVariants: List<Variant>): KeyBuilder {
            variants.addAll(newVariants)
            return this
        }

        fun build(): Key {
            return Key(propertyName, variants.toList().sorted())
        }
    }
}
