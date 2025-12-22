package base.boudicca.dateparser.dateparser.impl

import base.boudicca.dateparser.dateparser.DatePair
import base.boudicca.dateparser.dateparser.DateParserResult
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal data class ListOfDatePairSolution(val datePairs: List<DatePairSolution>) {
    fun isSolved(): Boolean = datePairs.isNotEmpty() && datePairs.all { it.isSolved() }

    fun toDateParserResult(timezone: ZoneId, clock: Clock): DateParserResult = DateParserResult(datePairs.map { it.toDatePair(timezone, clock) })
}

internal data class DatePairSolution(val startDate: DateSolution?, val endDate: DateSolution?) {
    fun toDatePair(timezone: ZoneId, clock: Clock): DatePair = DatePair(
        startDate!!.toOffsetDateTime(timezone, clock),
        endDate?.toOffsetDateTime(timezone, clock),
    )

    fun plusDataFrom(other: DatePairSolution): DatePairSolution = DatePairSolution(
        startDate?.plusDateAndTimeFrom(other.startDate),
        if (other.endDate != null) {
            (endDate ?: startDate)?.plusDateAndTimeFrom(other.endDate)
        } else {
            endDate
        },
    )

    fun isSolved(): Boolean = startDate != null && startDate.isSolved() && (endDate == null || endDate.isSolved())
}

internal data class DateSolution(val day: Int?, val month: Int?, val year: Int?, val hours: Int?, val minutes: Int?, val seconds: Int?, val originalTokens: List<Tokens>) {
    fun isSolved(): Boolean = day != null && month != null

    fun toOffsetDateTime(timezone: ZoneId, clock: Clock): OffsetDateTime = toLocalDate(clock).atTime(toLocalTime()).atZone(timezone).toOffsetDateTime()

    private fun toLocalDate(clock: Clock): LocalDate = LocalDate.of(year ?: tryGuessYear(clock, day!!, month!!).toInt(), month!!, day!!)

    private fun toLocalTime(): LocalTime = LocalTime.of(hours ?: 0, minutes ?: 0, seconds ?: 0)

    fun plusDateFrom(otherSolution: DateSolution?): DateSolution = DateSolution(
        day ?: otherSolution?.day,
        month ?: otherSolution?.month,
        year ?: otherSolution?.year,
        hours,
        minutes,
        seconds,
        originalTokens,
    )

    fun plusDateAndTimeFrom(otherSolution: DateSolution?): DateSolution = DateSolution(
        day ?: otherSolution?.day,
        month ?: otherSolution?.month,
        year ?: otherSolution?.year,
        hours ?: otherSolution?.hours,
        minutes ?: otherSolution?.minutes,
        seconds ?: otherSolution?.seconds,
        originalTokens,
    )

    fun hasNothingInCommonWith(other: DateSolution): Boolean = !(
        this.day != null && other.day != null ||
            this.month != null && other.month != null ||
            this.year != null && other.year != null ||
            this.hours != null && other.hours != null ||
            this.minutes != null && other.minutes != null ||
            this.seconds != null && other.seconds != null
        )

    @Suppress("LongParameterList")
    companion object {
        fun create(day: Token?, month: Token?, year: Token?, hours: Token?, minutes: Token?, seconds: Token?, originalTokens: List<Tokens>): DateSolution {
            val parsedDay = day?.value?.toInt()
            val parsedMonth =
                if (month != null) {
                    month.value.toIntOrNull() ?: MonthMappings.mapMonthToInt(month.value)
                        ?: throw IllegalArgumentException("cannot convert value $month to a month, this seems to be a parser bug, please report")
                } else {
                    null
                }
            val parsedYear = if (year != null) fixYear(year.value.toInt()) else null
            return DateSolution(
                parsedDay,
                parsedMonth,
                parsedYear,
                hours?.value?.toInt(),
                minutes?.value?.toInt(),
                seconds?.value?.toInt(),
                originalTokens,
            )
        }

        @Suppress("MagicNumber")
        private fun tryGuessYear(clock: Clock, day: Int, month: Int): Int {
            val now = LocalDate.now(clock)
            val date = LocalDate.of(now.year, month, day)
            // if the date with the current year would be in the past (with 2 months grace period) then it probably is next year
            return if (date < now.minusMonths(2)) {
                now.year + 1
            } else {
                now.year
            }
        }

        @Suppress("MagicNumber")
        private fun fixYear(year: Int): Int = if (year < 70) { // we get some problems in the year 2070 with this...
            2000 + year
        } else if (year < 100) {
            1900 + year
        } else {
            year
        }
    }
}
