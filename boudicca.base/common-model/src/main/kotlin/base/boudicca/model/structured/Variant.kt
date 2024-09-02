package base.boudicca.model.structured

data class Variant(val variantName: String, val variantValue: String) : Comparable<Variant> {
    fun toKeyString(): String {
        return "${variantName}=${variantValue}"
    }

    companion object {
        val COMPARATOR = compareBy<Variant> { it.variantName }.thenBy { it.variantValue }
    }

    override fun compareTo(other: Variant): Int {
        return COMPARATOR.compare(this, other)
    }
}