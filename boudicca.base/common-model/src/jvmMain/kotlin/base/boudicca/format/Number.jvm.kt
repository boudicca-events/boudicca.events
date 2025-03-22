package base.boudicca.format

import java.math.BigDecimal

actual object NumberParser {
    actual fun parseNumber(value: String): Number {
        return BigDecimal(value)
    }
}
