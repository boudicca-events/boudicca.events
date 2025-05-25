package base.boudicca.api.eventcollector.dateparser

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

class DateParserTest {
    @Test
    fun invalidDatesThrowExceptions() {
        assertThrows<IllegalArgumentException> {
            dateParser { }
        }
    }

    @Test
    fun simpleNumberDates() {
        assertDates(dateParser {
            dayMonthYear("25.04.1992")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 04 1992")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25-04-1992")
        }, "1992-04-25T00:00+02:00")
    }

    @Test
    fun simpleTextDates() {
        assertDates(dateParser {
            dayMonthYear("25. April 1992")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 April 1992")
        }, "1992-04-25T00:00+02:00")


        assertDates(dateParser {
            dayMonthYear("25 Jänner 1992")
        }, "1992-01-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Januar 1992")
        }, "1992-01-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Februar 1992")
        }, "1992-02-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 März 1992")
        }, "1992-03-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 April 1992")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Mai 1992")
        }, "1992-05-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Juni 1992")
        }, "1992-06-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Juli 1992")
        }, "1992-07-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 August 1992")
        }, "1992-08-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 September 1992")
        }, "1992-09-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Oktober 1992")
        }, "1992-10-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 November 1992")
        }, "1992-11-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Dezember 1992")
        }, "1992-12-25T00:00+01:00")
    }

    @Test
    fun shortTextDates() {
        assertDates(dateParser {
            dayMonthYear("25 Jän 1992")
        }, "1992-01-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Jan 1992")
        }, "1992-01-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Feb 1992")
        }, "1992-02-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Mär 1992")
        }, "1992-03-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Apr 1992")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Mai 1992")
        }, "1992-05-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Jun 1992")
        }, "1992-06-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Jul 1992")
        }, "1992-07-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Aug 1992")
        }, "1992-08-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Sep 1992")
        }, "1992-09-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25 Okt 1992")
        }, "1992-10-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Nov 1992")
        }, "1992-11-25T00:00+01:00")
        assertDates(dateParser {
            dayMonthYear("25 Dez 1992")
        }, "1992-12-25T00:00+01:00")
    }

    @Test
    fun simpleDateTimes() {
        assertDates(dateParser {
            dayMonthYear("25.04.1992")
            time("10:00")
        }, "1992-04-25T10:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25.04.1992")
            time("10.00")
        }, "1992-04-25T10:00+02:00")
    }

    @Test
    fun dateTimesWithNoiseInTime() {
        assertDates(dateParser {
            dayMonthYear("25.04.1992")
            time("10:00 Uhr")
        }, "1992-04-25T10:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25.04.1992")
            time("10 00 Uhr")
        }, "1992-04-25T10:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25.04.1992")
            time("Uhr 10 00")
        }, "1992-04-25T10:00+02:00")
    }

    @Test
    fun datesWithNoiseInDate() {
        assertDates(dateParser {
            dayMonthYear("Sa, 25.04.1992")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25.04.1992 Sa")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("Samstag 25.04.1992")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("Samstag 25.04.1992 whatever")
        }, "1992-04-25T00:00+02:00")
    }

    @Test
    fun datesWithShorthandYear() {
        assertDates(dateParser {
            dayMonthYear("25.04.92")
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25.04.25")
        }, "2025-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25.04.00")
        }, "2000-04-25T00:00+02:00")
        assertDates(dateParser {
            dayMonthYear("25.04.99")
        }, "1999-04-25T00:00+02:00")
    }

    @Test
    fun parseSimpleLocalDate() {
        assertThat(localDateParser {
            dayMonthYear("17.5.2025")
        }).isEqualTo(LocalDate.of(2025, 5, 17))
        assertThat(localDateParser {
            dayMonthYear("17 5 2025")
        }).isEqualTo(LocalDate.of(2025, 5, 17))
    }

    @Test
    fun parseSimpleLocalTime() {
        assertThat(localTimeParser {
            time("17:00")
        }).isEqualTo(LocalTime.of(17, 0))
        assertThat(localTimeParser {
            time("17 00 Uhr")
        }).isEqualTo(LocalTime.of(17, 0))
    }

    @Test
    fun parseDayMonthYearTime() {
        assertDates(dateParser {
            dayMonthYear().time().with(
                "25.04.1992 - 10 00"
            )
        }, "1992-04-25T10:00+02:00")
        assertDates(dateParser {
            dayMonthYear().time().with(
                "Fr. 25.04.1992 - 10 00"
            )
        }, "1992-04-25T10:00+02:00")
    }

    @Test
    fun parseYearMonthDay() {
        assertDates(dateParser {
            token().year().month().day().with(
                "1992-04-25"
            )
        }, "1992-04-25T00:00+02:00")
        assertDates(dateParser {
            token().year().month().day().with(
                "Fr. 1992.04.25"
            )
        }, "1992-04-25T00:00+02:00")
    }

    @Test
    fun parseYearMonthDayHoursMinutesSeconds() {
        assertDates(dateParser {
            token().year().month().day().hours().minutes().seconds().with(
                "1992-04-25 02:04:06"
            )
        }, "1992-04-25T02:04:06+02:00")
        assertDates(dateParser {
            token().year().month().day().hours().minutes().seconds().with(
                "1992-04-25 02 04 06"
            )
        }, "1992-04-25T02:04:06+02:00")
    }

    @Test
    fun parseNoiseHeavyDateTime() {
        assertDates(dateParser {
            dayMonthYear().time().with(
                "es ist am 25 April 1992 um 2:40 Uhr"
            )
        }, "1992-04-25T02:40+02:00")
        assertDates(dateParser {
            dayMonthYear().time().with(
                "es ist am 25ten April im Jahre 1992 um 2 Uhr und 40 Minuten"
            )
        }, "1992-04-25T02:40+02:00")
    }

    @Test
    fun parseSimpleAnyTime() {
        assertThat(localTimeParser {
            any(
                "19:30"
            )
        }).isEqualTo(LocalTime.of(19, 30))
    }

    private fun assertDates(actual: OffsetDateTime, expected: String) {
        assertThat(actual.toString()).isEqualTo(expected)
    }
}
