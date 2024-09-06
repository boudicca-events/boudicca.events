package base.boudicca.format

import java.math.BigDecimal

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