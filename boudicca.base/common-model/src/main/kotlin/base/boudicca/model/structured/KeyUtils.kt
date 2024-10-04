package base.boudicca.model.structured

/**
 * utils for parsing keys
 */
object KeyUtils {
    @Throws(IllegalArgumentException::class)
    fun toStructuredKeyValuePairs(map: Map<String, String>): Map<Key, String> {
        val foundKeys = mutableSetOf<Key>()
        val result = mutableMapOf<Key, String>()

        for (entry in map.entries) {
            val parsedKey = parseKey(entry.key)
            if (foundKeys.contains(parsedKey)) {
                throw IllegalArgumentException("duplicated key $parsedKey found in entry/event $map")
            }
            foundKeys.add(parsedKey)
            result[parsedKey] = entry.value
        }

        return result
    }

    fun toFlatKeyValuePairs(data: Map<Key, String>): Map<String, String> {
        return data.mapKeys { it.key.toKeyString() }
    }

    @Throws(IllegalArgumentException::class)
    fun parseKey(propertyKey: String): Key {
        val trimmedPropertyKey = propertyKey.trim()
        if (trimmedPropertyKey.isEmpty()) {
            throw IllegalArgumentException("given keyfilter was empty")
        }
        val split = trimmedPropertyKey.split(":")
        val variants = split
            .drop(1)
            .map {
                val splittedVariant = it.split("=")
                if (splittedVariant.size == 1) {
                    throw IllegalArgumentException("variant $it does not contain a \"=\"")
                }
                if (splittedVariant.size > 2) {
                    throw IllegalArgumentException("variant $it does contain too many \"=\"")
                }
                if (splittedVariant[0] == "*" || splittedVariant[0].isEmpty()) {
                    throw IllegalArgumentException("variant $it has an invalid variant name")
                }
                Variant(splittedVariant[0], splittedVariant[1])
            }
            .sorted()
        return Key(split[0], variants)
    }
}