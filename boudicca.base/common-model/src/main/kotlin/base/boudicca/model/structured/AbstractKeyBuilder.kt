package base.boudicca.model.structured

abstract class AbstractKeyBuilder<T>(
    private val name: String,
) {
    private val variants = mutableListOf<Variant>()

    fun withVariant(
        variantName: String,
        variantValue: String,
    ): AbstractKeyBuilder<T> = withVariant(Variant(variantName, variantValue))

    fun withVariant(variant: Variant): AbstractKeyBuilder<T> {
        variants.add(variant)
        return this
    }

    fun withVariants(newVariants: List<Variant>): AbstractKeyBuilder<T> {
        variants.addAll(newVariants)
        return this
    }

    fun build(): T = build(name, variants)

    abstract fun build(
        name: String,
        variants: List<Variant>,
    ): T
}
