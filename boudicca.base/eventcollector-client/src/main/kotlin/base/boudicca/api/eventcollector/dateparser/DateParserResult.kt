package base.boudicca.api.eventcollector.dateparser

import java.time.OffsetDateTime

data class DateParserResult(val dates: List<DatePair>) {
    fun single(): DatePair {
        return dates.single()
    }
}

data class DatePair(val startDate: OffsetDateTime, val endDate: OffsetDateTime?) {
    constructor(startDate: OffsetDateTime) : this(startDate, null)
}
