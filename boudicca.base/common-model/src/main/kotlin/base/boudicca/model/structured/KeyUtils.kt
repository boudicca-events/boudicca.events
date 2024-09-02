package base.boudicca.model.structured

object KeyUtils {
    @Throws(IllegalArgumentException::class)
    fun toStructuredKeyValuePairs(map: Map<String, String>): Map<Key, String> {
        return map.mapKeys {
            val (propertyName, variants) = parseKey(it.key)
            Key(propertyName, variants)
        }
    }

    fun toFlatKeyValuePairs(data: Map<Key, String>): Map<String, String> {
        return data.mapKeys { it.key.toKeyString() }
    }

    @Throws(IllegalArgumentException::class)
    fun parseKey(propertyKey: String): Pair<String, List<Variant>> {
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
                    throw IllegalArgumentException("variant $it does not contain too many \"=\"")
                }
                if (splittedVariant[0] == "*" || splittedVariant[0].isEmpty()) {
                    throw IllegalArgumentException("variant $it has an invalid variant name")
                }
                Variant(splittedVariant[0], splittedVariant[1])
            }
            .sorted()
        return Pair(split[0], variants)
    }
}