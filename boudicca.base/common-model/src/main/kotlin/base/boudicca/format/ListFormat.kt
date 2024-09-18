package base.boudicca.format

object ListFormat {
    fun parseFromString(value: String): List<String> {
        val result = mutableListOf<String>()
        val currentValue = StringBuilder()
        var i = 0
        while (i < value.length) {
            val c = value[i]
            i++
            if (c == '\\') {
                if (i < value.length) {
                    val escapedC = value[i]
                    i++
                    if (escapedC == ',' || escapedC == '\\') {
                        currentValue.append(escapedC)
                    }
                }
            } else {
                if (c == ',') {
                    result.add(currentValue.toString())
                    currentValue.clear()

                } else {
                    currentValue.append(c)
                }
            }
        }
        result.add(currentValue.toString())
        return result
    }

    @Throws(IllegalArgumentException::class)
    fun parseToString(value: List<String>): String {
        if (value.isEmpty()) {
            throw IllegalArgumentException("and empty list cannot be parsed to a string value")
        }
        return value.joinToString(",") { it.replace("\\", "\\\\").replace(",", "\\,") }
    }
}