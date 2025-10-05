package base.boudicca.api.eventcollector.dateparser

import assertk.assertThat
import assertk.assertions.isEqualTo
import base.boudicca.dateparser.dateparser.DatePair
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserConfig
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneId

class DateParserTest {
    companion object {
        val testDates: List<Triple<List<String>, List<DatePair>, DateParserConfig>> = listOf(
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

            //english months
            testDate("25 January 1992", "1992-01-25T00:00+01:00"),
            testDate("25 February 1992", "1992-02-25T00:00+01:00"),
            testDate("25 March 1992", "1992-03-25T00:00+01:00"),
            testDate("25 April 1992", "1992-04-25T00:00+02:00"),
            testDate("25 May 1992", "1992-05-25T00:00+02:00"),
            testDate("25 June 1992", "1992-06-25T00:00+02:00"),
            testDate("25 July 1992", "1992-07-25T00:00+02:00"),
            testDate("25 August 1992", "1992-08-25T00:00+02:00"),
            testDate("25 September 1992", "1992-09-25T00:00+02:00"),
            testDate("25 October 1992", "1992-10-25T00:00+01:00"),
            testDate("25 November 1992", "1992-11-25T00:00+01:00"),
            testDate("25 December 1992", "1992-12-25T00:00+01:00"),
            testDate("January 25, 1992", "1992-01-25T00:00+01:00"),
            testDate("February 25, 1992", "1992-02-25T00:00+01:00"),
            testDate("March 25, 1992", "1992-03-25T00:00+01:00"),
            testDate("April 25, 1992", "1992-04-25T00:00+02:00"),
            testDate("May 25, 1992", "1992-05-25T00:00+02:00"),
            testDate("June 25, 1992", "1992-06-25T00:00+02:00"),
            testDate("July 25, 1992", "1992-07-25T00:00+02:00"),
            testDate("August 25, 1992", "1992-08-25T00:00+02:00"),
            testDate("September 25, 1992", "1992-09-25T00:00+02:00"),
            testDate("October 25, 1992", "1992-10-25T00:00+01:00"),
            testDate("November 25, 1992", "1992-11-25T00:00+01:00"),
            testDate("December 25, 1992", "1992-12-25T00:00+01:00"),

            //date and time
            testDate(listOf("25.04.1992", "10:00"), "1992-04-25T10:00+02:00"),
            testDate(listOf("25.04.1992", "10.00"), "1992-04-25T10:00+02:00"),
            testDate(listOf("25.04.1992", "10:00 Uhr"), "1992-04-25T10:00+02:00"),
            testDate(listOf("25.04.1992", "10 00 Uhr"), "1992-04-25T10:00+02:00"),
            testDate(listOf("25.04.1992", "Uhr 10 00"), "1992-04-25T10:00+02:00"),
            testDate("10.04.1992 10:05", "1992-04-10T10:05+02:00"),
            testDate("11.04.1992 10:04:05", "1992-04-11T10:04:05+02:00"),
            testDate("11:04:1992 10:04:05", "1992-04-11T10:04:05+02:00"), //this is weird, if it breaks remove test

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
            testDate(listOf("25.04.1992", "10.05"), "1992-04-25T10:05+02:00"),
            testDate(listOf("10 04 1992", "10:05"), "1992-04-10T10:05+02:00"),
            testDate("10 04 1992  10:05", "1992-04-10T10:05+02:00"),
            testDate("10 04 1992 10:05", "1992-04-10T10:05+02:00"),
            testDate("10 04 1992 10 05", "1992-04-10T10:05+02:00"),

            //grouping tests
            testDate("10 04 03  10 30", "2003-04-10T10:30+02:00"),
            //TODO more variations of grouping

            //until tests
            testDate("Di. 17.06.2025 09:00 - 10:00 Uhr", pair("2025-06-17T09:00+02:00", "2025-06-17T10:00+02:00")),
            testDate(
                "Di. 17.06.2025 09:00 - 18.06.2025 10:00 Uhr", pair("2025-06-17T09:00+02:00", "2025-06-18T10:00+02:00")
            ),
            testDate("Mi, 22. Apr 26 - So, 26. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-26T00:00+02:00")),
            testDate("Mi, 22. Apr 26 - So, 28. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-28T00:00+02:00")),
            testDate("Mi, 22. Apr - So, 26. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-26T00:00+02:00")),
            testDate("Mi, 22. Apr - So, 28. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-28T00:00+02:00")),
            testDate("Mi, 22. Apr bis So, 28. Apr 26", pair("2026-04-22T00:00+02:00", "2026-04-28T00:00+02:00")),
            testDate("16 Uhr bis 18 Uhr 25.7.25", pair("2025-07-25T16:00+02:00", "2025-07-25T18:00+02:00")),
            testDate("16 bis 18 Uhr 25.7.25", pair("2025-07-25T16:00+02:00", "2025-07-25T18:00+02:00")),

            //grouping in until tests
            testDate(listOf("25.04.1992 10.05 - 10.15"), pair("1992-04-25T10:05+02:00", "1992-04-25T10:15+02:00")),
            testDate(
                listOf("08.04.1992 10.05 - 10.04.1992 10.15"), pair("1992-04-08T10:05+02:00", "1992-04-10T10:15+02:00")
            ),
            testDate(
                listOf("08.04.1992 10.05 - 10 04 1992 10:15"), pair("1992-04-08T10:05+02:00", "1992-04-10T10:15+02:00")
            ),
            testDate(
                listOf("08.04.1992 10.05 - 10.04.1992     10.15"),
                pair("1992-04-08T10:05+02:00", "1992-04-10T10:15+02:00")
            ),

            //multi-date text
            testDate(
                listOf("13.07.2025 + 14.07.2025"), listOf(
                    pair("2025-07-13T00:00+02:00", null),
                    pair("2025-07-14T00:00+02:00", null),
                )
            ),
            testDate(
                listOf("13.07.2025 und 14.07.2025"), listOf(
                    pair("2025-07-13T00:00+02:00", null),
                    pair("2025-07-14T00:00+02:00", null),
                )
            ),
            testDate(
                listOf("13.07.2025, 14.07.2025"), listOf(
                    pair("2025-07-13T00:00+02:00", null),
                    pair("2025-07-14T00:00+02:00", null),
                )
            ),
            testDate(
                listOf("25.07 und 26.07.2025"), listOf(
                    pair("2025-07-25T00:00+02:00", null),
                    pair("2025-07-26T00:00+02:00", null),
                )
            ),
            testDate(
                listOf("25. und 26.07.2025"), listOf(
                    pair("2025-07-25T00:00+02:00", null),
                    pair("2025-07-26T00:00+02:00", null),
                )
            ),
            testDate(
                listOf("25. und 26. + 27.07.2025 10:30"), listOf(
                    pair("2025-07-25T10:30+02:00", null),
                    pair("2025-07-26T10:30+02:00", null),
                    pair("2025-07-27T10:30+02:00", null),
                )
            ),
            testDate(
                listOf("13. und 27.07., 10.08. und 24.08.25 \"SOMMERBETRIEBSTAGE\" - Museumsbahn Ampflwang-Timelkam"),
                listOf(
                    pair("2025-07-13T00:00+02:00", null),
                    pair("2025-07-27T00:00+02:00", null),
                    pair("2025-08-10T00:00+02:00", null),
                    pair("2025-08-24T00:00+02:00", null),
                )
            ),
            testDate(
                listOf("16:00 - 18:00 und 19:00 - 21:00 am 25.7.2025"), listOf(
                    pair("2025-07-25T16:00+02:00", "2025-07-25T18:00+02:00"),
                    pair("2025-07-25T19:00+02:00", "2025-07-25T21:00+02:00"),
                )
            ),
            testDate(
                listOf("16:00 - 18:00 23.7.2025 und 19:00 - 21:00 am 25.7.2025"), listOf(
                    pair("2025-07-23T16:00+02:00", "2025-07-23T18:00+02:00"),
                    pair("2025-07-25T19:00+02:00", "2025-07-25T21:00+02:00"),
                )
            ),
            testDate(
                listOf("16 Uhr und 18 Uhr 25.7.25"), listOf(
                    pair("2025-07-25T16:00+02:00", null),
                    pair("2025-07-25T18:00+02:00", null),
                )
            ),
            testDate(
                listOf("16 und 18 Uhr 25.7.25"), listOf(
                    pair("2025-07-25T16:00+02:00", null),
                    pair("2025-07-25T18:00+02:00", null),
                )
            ),

            //multi-date with nested range
            testDate(
                listOf("25. und 26. + 27.07.2025 10:30 - 11:00"), listOf(
                    pair("2025-07-25T10:30+02:00", "2025-07-25T11:00+02:00"),
                    pair("2025-07-26T10:30+02:00", "2025-07-26T11:00+02:00"),
                    pair("2025-07-27T10:30+02:00", "2025-07-27T11:00+02:00"),
                )
            ),

            //year guessing
            testDateWithFixedClock(
                "17. September – 3. Oktober",
                pair("2025-09-17T00:00+02:00", "2025-10-03T00:00+02:00"),
                "2025-07-17T19:00+02:00"
            ),
            testDateWithFixedClock(
                "17. September – 3. Oktober",
                pair("2026-09-17T00:00+02:00", "2026-10-03T00:00+02:00"),
                "2025-12-31T19:00+02:00"
            ),

            //weird orders, but only parseable in one format (no D.M vs M.D issues)
            testDate("Okt 1 2025", "2025-10-01T00:00+02:00"),
            testDate("2025 Okt 1 19:00", "2025-10-01T19:00+02:00"),

            //only solvable with a given day/month or month/day order
            testDateWithDayMonthOrder(
                "1 2 2025",
                pair("2025-02-01T00:00+01:00", null),
                DateParserConfig.DayMonthOrder.DAY_MONTH
            ),
            testDateWithDayMonthOrder(
                "1 2 2025",
                pair("2025-01-02T00:00+01:00", null),
                DateParserConfig.DayMonthOrder.MONTH_DAY
            ),

            //more misc tests found from collectors
            testDate("Sep. 29 – Okt. 3, 2025 All Day Event", pair("2025-09-29T00:00+02:00", "2025-10-03T00:00+02:00")),
            testDate("Sep. 22 09.00 – 24, 2025", pair("2025-09-22T09:00+02:00", "2025-09-24T00:00+02:00")),
            testDate("Juni 6 19.00 – 8, 2025", pair("2025-06-06T19:00+02:00", "2025-06-08T00:00+02:00")),
            testDate(
                listOf("01.-04.09.25", "Einlass: 09:30 Uhr"), pair("2025-09-01T09:30+02:00", "2025-09-04T09:30+02:00")
            ),
            testDate(
                "03.05.2024 19:00 - 04.05.2024", pair("2024-05-03T19:00+02:00", "2024-05-04T00:00+02:00")
            ),
            testDate(
                listOf("Date(s) - Do. 18.09.2025", "18:00 - 20:00"),
                pair("2025-09-18T18:00+02:00", "2025-09-18T20:00+02:00")
            ),
            testDate(
                listOf("Date(s) - So. 21.12.2025", "14:00 - 20:00"),
                pair("2025-12-21T14:00+01:00", "2025-12-21T20:00+01:00")
            ),
            testDateWithFixedClock(
                "17. September um 19:00 – 3. Oktober um 18:00 CEST",
                pair("2025-09-17T19:00+02:00", "2025-10-03T18:00+02:00"),
                "2025-07-17T19:00+02:00"
            )
        )

        fun testDate(
            dateText: String,
            expected: String
        ): Triple<List<String>, List<DatePair>, DateParserConfig> {
            return testDate(listOf(dateText), listOf(DatePair(OffsetDateTime.parse(expected))))
        }

        fun testDate(dateText: String, expected: DatePair): Triple<List<String>, List<DatePair>, DateParserConfig> {
            return testDate(listOf(dateText), listOf(expected))
        }

        fun testDate(
            dateText: List<String>,
            expected: DatePair
        ): Triple<List<String>, List<DatePair>, DateParserConfig> {
            return testDate(dateText, listOf(expected))
        }

        fun testDate(dateText: List<String>, expected: String): Triple<List<String>, List<DatePair>, DateParserConfig> {
            return testDate(dateText, pair(expected, null))
        }

        fun testDate(
            dateText: List<String>,
            expected: List<DatePair>,
            config: DateParserConfig? = null
        ): Triple<List<String>, List<DatePair>, DateParserConfig> {
            return Triple(
                dateText, expected, config ?: DateParserConfig(
                    alwaysPrintDebugTracing = true
                )
            )
        }

        private fun pair(startDate: String, endDate: String?): DatePair {
            return DatePair(OffsetDateTime.parse(startDate), endDate?.run { OffsetDateTime.parse(this) })
        }

        private fun testDateWithFixedClock(
            dateText: String,
            expected: DatePair,
            fixedClockDate: String
        ): Triple<List<String>, List<DatePair>, DateParserConfig> {
            val clock = Clock.fixed(OffsetDateTime.parse(fixedClockDate).toInstant(), ZoneId.of("Europe/Vienna"))
            return testDate(
                listOf(dateText), listOf(expected), DateParserConfig(
                    alwaysPrintDebugTracing = true,
                    clock = clock
                )
            )
        }

        private fun testDateWithDayMonthOrder(
            dateText: String,
            expected: DatePair,
            dayMonthOrder: DateParserConfig.DayMonthOrder
        ): Triple<List<String>, List<DatePair>, DateParserConfig> {
            return testDate(
                listOf(dateText), listOf(expected), DateParserConfig(
                    dayMonthOrder = dayMonthOrder,
                    alwaysPrintDebugTracing = true,
                )
            )
        }
    }


    @ParameterizedTest
    @FieldSource("testDates")
    fun testDateParser(testCase: Triple<List<String>, List<DatePair>, DateParserConfig>) {
        val (actual, expected) = testCase
        val result = DateParser.parse(actual, testCase.third)
        assertThat(result.dates.size).isEqualTo(expected.size)
        for (i in expected.indices) {
            assertThat(result.dates[i].startDate).isEqualTo(expected[i].startDate)
            assertThat(result.dates[i].endDate).isEqualTo(expected[i].endDate)
        }
    }
}
