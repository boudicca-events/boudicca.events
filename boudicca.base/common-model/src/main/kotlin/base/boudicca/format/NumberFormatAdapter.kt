package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import java.math.BigDecimal

/**
 * parsing utils to get string values to format number and back
 *
 * numbers are everything a BigDecimal can parse for now, see constructor of [BigDecimal]
 *
 * all methods may throw exceptions on wrong formatted values
 */
class NumberFormatAdapter : AbstractFormatAdapter<Number>(VariantConstants.FormatVariantConstants.NUMBER_FORMAT_NAME) {
    override fun fromString(value: String): Number {
        try {
            return BigDecimal(value)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("could not parse string $value to number")
        }
    }

    override fun convertToString(value: Number): String = value.toString()
}
