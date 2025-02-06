package base.boudicca.model.structured

import kotlin.math.min

/**
 * represents a parsed Key of a Key-Value pair which consists of the name and all the variants (which are sorted canonically)
 */
data class Key(val name: String, val variants: List<Variant>) : Comparable<Key> {

    /**
     * get the key string representation of this key for using in an unstructured/serialized event
     */
    fun toKeyString(): String {
        if (variants.isEmpty()) {
            return name
        }
        return name + ":" + variants.joinToString(":") { it.toKeyString() }
    }

    companion object {
        val COMPARATOR = compareBy<Key> { it.name }
            .thenComparing { o1, o2 ->
                for (i in 0..<min(o1.variants.size, o2.variants.size)) {
                    val result = o1.variants[i].compareTo(o2.variants[i])
                    if (result != 0) {
                        return@thenComparing result
                    }
                }
                return@thenComparing o1.variants.size.compareTo(o2.variants.size)
            }

        fun parse(keyFilter: String): Key {
            return KeyUtils.parseKey(keyFilter)
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

    override fun toString() = toKeyString()
}
