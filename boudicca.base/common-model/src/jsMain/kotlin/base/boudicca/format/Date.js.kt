package base.boudicca.format

actual typealias Date = kotlin.js.Date

actual object DateParser {
    actual fun parseDate(date: String): Date {
        return Date(date)
    }

    actual fun dateToString(date: Date): String {
        return date.toISOString()
    }
}
