package base.boudicca.dateparser.dateparser.impl

import base.boudicca.dateparser.dateparser.DatePair
import base.boudicca.dateparser.dateparser.DateParserResult
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal data class ListOfDatePairSolution(
    val datePairs: List<DatePairSolution>
) {
    fun isSolved(): Boolean {
        return datePairs.isNotEmpty() && datePairs.all { it.isSolved() }
    }

    fun toDateParserResult(timezone: ZoneId): DateParserResult {
        return DateParserResult(datePairs.map { it.toDatePair(timezone) })
    }
}

internal data class DatePairSolution(
    val startDate: DateSolution?,
    val endDate: DateSolution?,
) {
    fun toDatePair(timezone: ZoneId): DatePair {
        return DatePair(
            startDate!!.toOffsetDateTime(timezone), endDate?.toOffsetDateTime(timezone)
        )
    }

    fun plusDataFrom(other: DatePairSolution): DatePairSolution {
        return DatePairSolution(
            startDate?.plusDateAndTimeFrom(other.startDate),
            if (other.endDate != null) {
                (endDate ?: startDate)?.plusDateAndTimeFrom(other.endDate)
            } else {
                endDate
            }
        )
    }

    fun isSolved(): Boolean {
        return startDate != null && startDate.isSolved() && (endDate == null || endDate.isSolved())
    }
}

internal data class DateSolution(
    val day: Int?, val month: Int?, val year: Int?, val hours: Int?, val minutes: Int?, val seconds: Int?,
    val originalTokens: List<Tokens>
) {
    fun isSolved(): Boolean {
        return day != null && month != null && year != null
    }

    fun toOffsetDateTime(timezone: ZoneId): OffsetDateTime {
        return toLocalDate().atTime(toLocalTime()).atZone(timezone).toOffsetDateTime()
    }

    private fun toLocalDate(): LocalDate {
        return LocalDate.of(year!!, month!!, day!!)
    }

    private fun toLocalTime(): LocalTime {
        return LocalTime.of(hours ?: 0, minutes ?: 0, seconds ?: 0)
    }

    fun plusDateFrom(otherSolution: DateSolution?): DateSolution {
        return DateSolution(
            day ?: otherSolution?.day,
            month ?: otherSolution?.month,
            year ?: otherSolution?.year,
            hours,
            minutes,
            seconds,
            originalTokens
        )
    }

    fun plusDateAndTimeFrom(otherSolution: DateSolution?): DateSolution {
        return DateSolution(
            day ?: otherSolution?.day,
            month ?: otherSolution?.month,
            year ?: otherSolution?.year,
            hours ?: otherSolution?.hours,
            minutes ?: otherSolution?.minutes,
            seconds ?: otherSolution?.seconds,
            originalTokens
        )
    }

    @Suppress("LongParameterList")
    companion object {
        fun create(
            day: Token?, month: Token?, year: Token?, hours: Token?, minutes: Token?, seconds: Token?,
            originalTokens: List<Tokens>
        ): DateSolution {
            return DateSolution(
                day?.value?.toInt(),
                if (month != null) month.value.toIntOrNull() ?: MonthMappings.mapMonthToInt(month.value)
                ?: throw IllegalArgumentException("cannot convert value $month to a month, this seems to be a parser bug, please report")
                else null,
                if (year != null) fixYear(year.value.toInt()) else null,
                hours?.value?.toInt(),
                minutes?.value?.toInt(),
                seconds?.value?.toInt(),
                originalTokens
            )
        }

        @Suppress("MagicNumber")
        private fun fixYear(year: Int): Int {
            return if (year < 70) { //we get some problems in the year 2070 with this...
                2000 + year
            } else if (year < 100) {
                1900 + year
            } else {
                year
            }
        }
    }
}
