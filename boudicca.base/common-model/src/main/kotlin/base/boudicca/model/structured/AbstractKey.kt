package base.boudicca.model.structured

import kotlin.math.min

/**
 * represents a parsed Key of a Key-Value pair which consists of the name and all the variants (which are sorted canonically)
 */
abstract class AbstractKey<T : AbstractKey<T>>(val name: String, variants: List<Variant> = emptyList()) : Comparable<T> {
    val variants = variants.sorted()

    init {
        internalValidate()
    }

    /**
     * get the key string representation of this key for using in an unstructured/serialized event
     */
    fun toKeyString(): String {
        if (variants.isEmpty()) {
            return name
        }
        return "$name:${variants.joinToString(":") { it.toKeyString() }}"
    }

    companion object {
        val COMPARATOR =
            compareBy<AbstractKey<*>> { it.name }.thenComparing { o1, o2 ->
                for (i in 0..<min(o1.variants.size, o2.variants.size)) {
                    val result = o1.variants[i].compareTo(o2.variants[i])
                    if (result != 0) {
                        return@thenComparing result
                    }
                }
                return@thenComparing o1.variants.size.compareTo(o2.variants.size)
            }
    }

    override fun compareTo(other: T): Int = COMPARATOR.compare(this, other)

    abstract fun toBuilder(): AbstractKeyBuilder<T>

    private fun internalValidate() {
        require(!name.contains(":")) { "key name $name is not allowed to contain a ':'" }
        validate()
    }

    open fun validate() {
        // child classes can override
    }

    override fun toString() = toKeyString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractKey<*>

        if (name != other.name) return false
        if (variants != other.variants) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + variants.hashCode()
        return result
    }
}
