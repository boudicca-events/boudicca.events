package base.boudicca.api.eventcollector.dateparser.impl

import java.time.LocalDate
import java.time.LocalTime


internal sealed interface SolutionComponent {
    fun isSolved(): Boolean {
        return false
    }
}

internal data class Date(
    val day: Int?, val month: Int?, val year: Int?
) : SolutionComponent {
    override fun isSolved(): Boolean {
        return day != null && month != null && year != null
    }

    fun toLocalDate(): LocalDate {
        return LocalDate.of(year!!, month!!, day!!)
    }

    companion object {
        fun create(
            day: Any, month: Any?, year: Any?
        ): Date {
            //TODO error catching?
            return Date(
                day.value.toInt(),
                if (month != null) month.value.toIntOrNull() ?: MonthMappings.mapMonthToInt(month.value)
                ?: throw IllegalArgumentException("blaa")
                else null,
                if (year != null) fixYear(year.value.toInt()) else null
            )
        }

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

internal data class Time(
    val hours: Int?, val minutes: Int?, val seconds: Int?
) : SolutionComponent {
    override fun isSolved(): Boolean {
        return hours != null
    }

    fun toLocalTime(): LocalTime {
        return LocalTime.of(hours!!, minutes ?: 0, seconds ?: 0)
    }

    companion object {
        fun create(
            hours: Any, minutes: Any?, seconds: Any?
        ): Time {
            //TODO error catching?
            return Time(
                hours.value.toInt(),
                minutes?.value?.toInt(),
                seconds?.value?.toInt(),
            )
        }
    }
}
