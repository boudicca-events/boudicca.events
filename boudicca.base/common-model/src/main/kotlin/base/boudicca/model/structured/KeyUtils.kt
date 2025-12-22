package base.boudicca.model.structured

/**
 * utils for parsing keys
 */
object KeyUtils {
    fun toStructuredKeyValuePairs(map: Map<String, String>): Map<Key, String> {
        val foundKeys = mutableSetOf<Key>()
        val result = mutableMapOf<Key, String>()

        for (entry in map.entries) {
            val parsedKey = parseKey(entry.key)
            require(!foundKeys.contains(parsedKey)) { "duplicated key $parsedKey found in entry/event $map" }
            foundKeys.add(parsedKey)
            result[parsedKey] = entry.value
        }

        return result
    }

    fun toFlatKeyValuePairs(data: Map<Key, String>): Map<String, String> = data.mapKeys { it.key.toKeyString() }

    fun parseKey(key: String): Key {
        val resultPair = parseKeyNameAndVariants(key)
        return Key(resultPair.first, resultPair.second)
    }

    fun parseKeyFilter(keyFilter: String): KeyFilter {
        val resultPair = parseKeyNameAndVariants(keyFilter)
        return KeyFilter(resultPair.first, resultPair.second)
    }

    private fun parseKeyNameAndVariants(propertyKey: String): Pair<String, List<Variant>> {
        val trimmedPropertyKey = propertyKey.trim()
        require(trimmedPropertyKey.isNotEmpty()) { "given keyfilter was empty" }
        val split = trimmedPropertyKey.split(":")
        val variants =
            split
                .drop(1)
                .map {
                    val splittedVariant = it.split("=")
                    require(splittedVariant.size > 1) { "variant $it does not contain a \"=\"" }
                    require(splittedVariant.size <= 2) { "variant $it does contain too many \"=\"" }
                    Variant(splittedVariant[0], splittedVariant[1])
                }.sorted()
        val resultPair = Pair(split[0], variants)
        return resultPair
    }
}
