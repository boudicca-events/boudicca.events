package base.boudicca.format

import base.boudicca.model.structured.VariantConstants

/**
 * parsing utils to get string values to format number and back
 *
 * numbers are everything a BigDecimal can parse for now, see constructor of [BigDecimal]
 *
 * all methods may throw exceptions on wrong formatted values
 */
class NumberFormatAdapter : AbstractFormatAdapter<Number>(VariantConstants.FormatVariantConstants.NUMBER_FORMAT_NAME) {
    override fun fromString(value: String): Number {
        return if (value.contains(".")) {
            value.toDouble()
        } else {
            value.toInt()
        }
    }

    override fun convertToString(value: Number): String {
        return value.toString()
    }
}
