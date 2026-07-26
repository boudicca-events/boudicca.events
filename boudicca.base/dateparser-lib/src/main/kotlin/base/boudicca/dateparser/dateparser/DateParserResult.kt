package base.boudicca.dateparser.dateparser

import java.time.OffsetDateTime

data class DateParserResult(
    val dates: List<DatePair>,
) {
    fun single(): DatePair = dates.single()

    operator fun plus(other: DateParserResult): DateParserResult = DateParserResult(dates + other.dates)
}

fun Collection<DateParserResult>.reduce(): DateParserResult = if (this.isNotEmpty()) this.reduce { r1, r2 -> r1 + r2 } else DateParserResult(emptyList())

data class DatePair(
    val startDate: OffsetDateTime,
    val endDate: OffsetDateTime?,
) {
    constructor(startDate: OffsetDateTime) : this(startDate, null)
}
