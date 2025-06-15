package base.boudicca.api.eventcollector.dateparser

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import java.time.OffsetDateTime

class DateParserTest {
    companion object {
        private val testDates = listOf(
            //simple dates
            testDate("25.04.1992", "1992-04-25T00:00+02:00"),
            testDate("25.04.1992", "1992-04-25T00:00+02:00"),
            testDate("25 04 1992", "1992-04-25T00:00+02:00"),
            testDate("25-04-1992", "1992-04-25T00:00+02:00"),

            //month as text
            testDate("25. April 1992", "1992-04-25T00:00+02:00"),
            testDate("25 April 1992", "1992-04-25T00:00+02:00"),
            testDate("25 Jänner 1992", "1992-01-25T00:00+01:00"),
            testDate("25 Januar 1992", "1992-01-25T00:00+01:00"),
            testDate("25 Februar 1992", "1992-02-25T00:00+01:00"),
            testDate("25 März 1992", "1992-03-25T00:00+01:00"),
            testDate("25 April 1992", "1992-04-25T00:00+02:00"),
            testDate("25 Mai 1992", "1992-05-25T00:00+02:00"),
            testDate("25 Juni 1992", "1992-06-25T00:00+02:00"),
            testDate("25 Juli 1992", "1992-07-25T00:00+02:00"),
            testDate("25 August 1992", "1992-08-25T00:00+02:00"),
            testDate("25 September 1992", "1992-09-25T00:00+02:00"),
            testDate("25 Oktober 1992", "1992-10-25T00:00+01:00"),
            testDate("25 November 1992", "1992-11-25T00:00+01:00"),
            testDate("25 Dezember 1992", "1992-12-25T00:00+01:00"),
            testDate("25 Jän 1992", "1992-01-25T00:00+01:00"),
            testDate("25 Jan 1992", "1992-01-25T00:00+01:00"),
            testDate("25 Feb 1992", "1992-02-25T00:00+01:00"),
            testDate("25 Mär 1992", "1992-03-25T00:00+01:00"),
            testDate("25 Apr 1992", "1992-04-25T00:00+02:00"),
            testDate("25 Mai 1992", "1992-05-25T00:00+02:00"),
            testDate("25 Jun 1992", "1992-06-25T00:00+02:00"),
            testDate("25 Jul 1992", "1992-07-25T00:00+02:00"),
            testDate("25 Aug 1992", "1992-08-25T00:00+02:00"),
            testDate("25 Sep 1992", "1992-09-25T00:00+02:00"),
            testDate("25 Okt 1992", "1992-10-25T00:00+01:00"),
            testDate("25 Nov 1992", "1992-11-25T00:00+01:00"),
            testDate("25 Dez 1992", "1992-12-25T00:00+01:00"),

            //date and time
            testDate(listOf("25.04.1992", "10:00"), "1992-04-25T10:00+02:00"),
            testDate(listOf("25.04.1992", "10.00"), "1992-04-25T10:00+02:00"),
            testDate(listOf("25.04.1992", "10:00 Uhr"), "1992-04-25T10:00+02:00"),
            testDate(listOf("25.04.1992", "10 00 Uhr"), "1992-04-25T10:00+02:00"),
            testDate(listOf("25.04.1992", "Uhr 10 00"), "1992-04-25T10:00+02:00"),

            //some noise
            testDate("Sa, 25.04.1992", "1992-04-25T00:00+02:00"),
            testDate("25.04.1992 Sa", "1992-04-25T00:00+02:00"),
            testDate("Samstag 25.04.1992", "1992-04-25T00:00+02:00"),
            testDate("Samstag 25.04.1992 whatever", "1992-04-25T00:00+02:00"),

            //short year
            testDate("25.04.92", "1992-04-25T00:00+02:00"),
            testDate("25.04.25", "2025-04-25T00:00+02:00"),
            testDate("25.04.00", "2000-04-25T00:00+02:00"),
            testDate("25.04.99", "1999-04-25T00:00+02:00"),

            //misc
            testDate("25.04.1992 - 10 00", "1992-04-25T10:00+02:00"),
            testDate("Fr. 25.04.1992 - 10 00", "1992-04-25T10:00+02:00"),
            testDate("1992-04-25", "1992-04-25T00:00+02:00"),
            testDate("1992-04-25 02:04:06", "1992-04-25T02:04:06+02:00"),
            testDate("es ist am 25 April 1992 um 2:40 Uhr", "1992-04-25T02:40+02:00"),
            testDate("es ist am 25ten April im Jahre 1992 um 2 Uhr und 40 Minuten", "1992-04-25T02:40+02:00"),
            testDate("25 April 1992 - 2:40 Uhr", "1992-04-25T02:40+02:00"),
            testDate("25.04.1992 - 2:40 Uhr", "1992-04-25T02:40+02:00"),
            testDate("Do. 10.07.2025 - 20:30", "2025-07-10T20:30+02:00"),

            //grouping tests
            testDate(listOf("25.04.1992", "10.05"), "1992-04-25T10:05+02:00"),
            testDate(listOf("10 04 1992", "10:05"), "1992-04-10T10:05+02:00"),
            testDate("10.04.1992 10:05", "1992-04-10T10:05+02:00"),
            testDate("10 04 1992  10:05", "1992-04-10T10:05+02:00"),
            //TODO more variations of grouping
            
            //range tests
            testDate("Di. 17.06.2025 09:00 - 10:00 Uhr", pair("2025-06-17T09:00+02:00", "2025-06-17T10:00+02:00")),
            testDate(
                "Di. 17.06.2025 09:00 - 18.06.2025 10:00 Uhr", pair("2025-06-17T09:00+02:00", "2025-06-18T10:00+02:00")
            ),
            testDate("Mi, 22. Apr 26 - So, 26. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-26T00:00+02:00")),
            testDate("Mi, 22. Apr 26 - So, 28. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-28T00:00+02:00")),
            testDate("Mi, 22. Apr - So, 26. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-26T00:00+02:00")),
            testDate("Mi, 22. Apr - So, 28. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-28T00:00+02:00")),
            testDate("Mi, 22. Apr bis So, 28. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-28T00:00+02:00")),
        )


        fun testDate(dateText: String, expected: String): Pair<List<String>, List<DatePair>> {
            return testDate(listOf(dateText), listOf(DatePair(OffsetDateTime.parse(expected))))
        }

        fun testDate(dateText: List<String>, expected: List<DatePair>): Pair<List<String>, List<DatePair>> {
            return Pair(dateText, expected)
        }

        fun testDate(dateText: String, expected: DatePair): Pair<List<String>, List<DatePair>> {
            return Pair(listOf(dateText), listOf(expected))
        }

        fun testDate(dateText: List<String>, expected: DatePair): Pair<List<String>, List<DatePair>> {
            return Pair(dateText, listOf(expected))
        }

        fun testDate(dateText: List<String>, expected: String): Pair<List<String>, List<DatePair>> {
            return Pair(dateText, listOf(DatePair(OffsetDateTime.parse(expected))))
        }

        private fun pair(startDate: String, endDate: String?): DatePair {
            return DatePair(OffsetDateTime.parse(startDate), endDate?.run { OffsetDateTime.parse(this) })
        }
    }


    @ParameterizedTest
    @FieldSource("testDates")
    fun testDateParser(testCase: Pair<List<String>, List<DatePair>>) {
        val (actual, expected) = testCase
        val result = DateParser(actual).parse()
        assertThat(result.dates.size).isEqualTo(expected.size)
        for (i in expected.indices) {
            assertThat(result.dates[i].startDate).isEqualTo(expected[i].startDate)
            assertThat(result.dates[i].endDate).isEqualTo(expected[i].endDate)
        }
    }
}
