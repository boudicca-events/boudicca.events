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

    fun toFlatKeyValuePairs(data: Map<Key, String>): Map<String, String> {
        return data.mapKeys { it.key.toKeyString() }
    }

    fun parseKey(propertyKey: String): Key {
        val trimmedPropertyKey = propertyKey.trim()
        require(trimmedPropertyKey.isNotEmpty()) { "given keyfilter was empty" }
        val split = trimmedPropertyKey.split(":")
        val variants = split.drop(1).map {
            val splittedVariant = it.split("=")
            require(splittedVariant.size > 1) { "variant $it does not contain a \"=\"" }
            require(splittedVariant.size <= 2) { "variant $it does contain too many \"=\"" }
            require(splittedVariant[0] != "*" && splittedVariant[0].isNotEmpty()) {
                "variant $it has an invalid variant name"
            }
            Variant(splittedVariant[0], splittedVariant[1])
        }.sorted()
        return Key(split[0], variants)
    }
}
