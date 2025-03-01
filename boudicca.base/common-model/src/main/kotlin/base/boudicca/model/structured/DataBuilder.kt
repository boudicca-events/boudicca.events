package base.boudicca.model.structured

// TODO rename this, but later
class DataBuilder(val name: String) {
    // all combinations of the form
    // description
    // description:lang=de
    // description:lang=de:format=markdown
    // etc.
    private val variantCombinations = mutableMapOf<List<Variant>, String>()

    fun variant(vararg variants: Variant, data: String) {
        variantCombinations[variants.toList()] = data
    }

    fun build(): Map<Key, String> {
        return variantCombinations.map { entry ->
            Key(name, entry.key) to entry.value
        }.toMap()
    }

}
