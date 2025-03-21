package base.boudicca.format


expect class Date

expect object DateParser {
    fun parseDate(date: String): Date
    fun dateToString(date: Date): String
}
