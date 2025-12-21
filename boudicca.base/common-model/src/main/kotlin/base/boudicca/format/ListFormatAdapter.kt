package base.boudicca.format

import base.boudicca.model.structured.VariantConstants

/**
 * parsing utils to get string values to format list and back
 *
 * list format is a "," separated list, meaning all "," and "\" occurring in list values must be escaped by an "\"
 * if your list ends with an empty element you need to add another "," because trailing empty values are ignored
 *
 * all methods may throw exceptions on wrong formatted values
 */
class ListFormatAdapter :
    AbstractFormatAdapter<List<String>>(VariantConstants.FormatVariantConstants.LIST_FORMAT_NAME) {
    fun fromStringOrNull(value: String?): List<String> = value?.let { fromString(it) } ?: emptyList()

    override fun fromString(value: String): List<String> {
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
        if (currentValue.toString() != "") {
            result.add(currentValue.toString())
        }
        return result
    }

    override fun convertToString(value: List<String>): String {
        val stringValue = value.joinToString(",") { it.replace("\\", "\\\\").replace(",", "\\,") }
        return if (value.isNotEmpty() && value.last() == "") {
            "$stringValue,"
        } else {
            stringValue
        }
    }
}
