package base.boudicca.format

import java.math.BigDecimal

/**
 * parsing utils to get string values to format number and back
 *
 * numbers are everything a BigDecimal can parse for now, see constructor of [BigDecimal]
 *
 * all methods may throw exceptions on wrong formatted values
 */
object NumberFormat {

    @Throws(IllegalArgumentException::class)
    fun parseFromString(value: String): Number {
        try {
            return BigDecimal(value)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("could not parse string $value to number")
        }
    }

    fun parseToString(value: Number): String {
        return value.toString()
    }
}
