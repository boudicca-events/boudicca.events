package base.boudicca.format

actual object NumberParser {
    actual fun parseNumber(value: String): Number {
        return value.toDouble()
    }
}
