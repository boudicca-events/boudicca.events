package base.boudicca.dateparser.dateparser.impl

const val MIN_MONTH_LENGTH_TO_MATCH = 3

internal object MonthMappings {
    @Suppress("MagicNumber")
    private val MONTH_MAPPINGS = mapOf(
        "januar" to 1,
        "jänner" to 1,
        "january" to 1,
        "februar" to 2,
        "february" to 2,
        "märz" to 3,
        "march" to 3,
        "april" to 4,
        "mai" to 5,
        "may" to 5,
        "juni" to 6,
        "june" to 6,
        "juli" to 7,
        "july" to 7,
        "august" to 8,
        "september" to 9,
        "oktober" to 10,
        "october" to 10,
        "november" to 11,
        "dezember" to 12,
        "december" to 12,
    )

    internal fun mapMonthToInt(month: String?): Int? {
        if (month == null || month.length < MIN_MONTH_LENGTH_TO_MATCH) {
            return null
        }
        val lowercaseMonth = month.lowercase()
        for (entry in MONTH_MAPPINGS) {
            if (entry.key.startsWith(lowercaseMonth)) {
                return entry.value
            }
        }
        return null
    }
}
