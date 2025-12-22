package base.boudicca.query.evaluator

import base.boudicca.SemanticKeys
import base.boudicca.model.Entry
import base.boudicca.query.AfterExpression
import base.boudicca.query.AndExpression
import base.boudicca.query.BeforeExpression
import base.boudicca.query.ContainsExpression
import base.boudicca.query.DurationLongerExpression
import base.boudicca.query.DurationShorterExpression
import base.boudicca.query.EqualsExpression
import base.boudicca.query.Expression
import base.boudicca.query.HasFieldExpression
import base.boudicca.query.IsInLastSecondsExpression
import base.boudicca.query.IsInNextSecondsExpression
import base.boudicca.query.NotExpression
import base.boudicca.query.OrExpression
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

abstract class AbstractEvaluatorTest {
    @Test
    fun simpleEquals() {
        val events = callEvaluator(EqualsExpression("name", "event1"))
        assertEquals(1, events.size)
        assertEquals("event1", events.first()["name"])
    }

    @Test
    fun simpleContains() {
        val events = callEvaluator(ContainsExpression("name", "event"))
        assertEquals(4, events.size)
    }

    @Test
    fun simpleEqualsWithList() {
        val events = callEvaluator(EqualsExpression("list", "val1"))
        assertEquals(1, events.size)
        assertEquals("listyEvent1", events.first()["name"])
    }

    @Test
    fun simpleContainsWithList() {
        val events = callEvaluator(ContainsExpression("list", "val"))
        assertEquals(2, events.size)
    }

    @Test
    fun simpleOr() {
        val events = callEvaluator(OrExpression(EqualsExpression("name", "event1"), EqualsExpression("name", "event2")))
        assertEquals(2, events.size)
    }

    @Test
    fun simpleAnd() {
        val events =
            callEvaluator(AndExpression(EqualsExpression("name", "somethingelse"), EqualsExpression("field", "wuuut")))
        assertEquals(1, events.size)
    }

    @Test
    fun simpleNot() {
        val events = callEvaluator(NotExpression(EqualsExpression("name", "event1")))
        assertEquals(5, events.size)
    }

    @Test
    fun orAndNot() {
        val events =
            callEvaluator(
                OrExpression(
                    NotExpression(
                        ContainsExpression("name", "event"),
                    ),
                    AndExpression(
                        EqualsExpression("name", "event1"),
                        EqualsExpression("field", "value1"),
                    ),
                ),
            )
        assertEquals(3, events.size)
    }

    @Test
    fun testCaseInsensitiveMatching() {
        var events =
            callEvaluator(
                EqualsExpression("field", "value"),
                listOf(
                    mapOf("field" to "value"),
                    mapOf("field" to "VAlue"),
                ),
            )
        assertEquals(2, events.size)

        events =
            callEvaluator(
                ContainsExpression("field", "value"),
                listOf(
                    mapOf("field" to "1value2"),
                    mapOf("field" to "1VAlue2"),
                ),
            )
        assertEquals(2, events.size)
    }

    @Test
    fun testCaseSensitiveFieldNameMatching() {
        val events =
            callEvaluator(
                EqualsExpression("field", "value"),
                listOf(
                    mapOf("field" to "value"),
                    mapOf("FIELD" to "value"),
                ),
            )
        assertEquals(1, events.size)
    }

    @Test
    fun testStarFieldName() {
        var events =
            callEvaluator(
                EqualsExpression("*", "value"),
                listOf(
                    mapOf("field" to "value"),
                    mapOf("otherfield" to "value"),
                ),
            )
        assertEquals(2, events.size)

        events =
            callEvaluator(
                ContainsExpression("*", "value"),
                listOf(
                    mapOf("field" to "1value2"),
                    mapOf("otherfield" to "1value2"),
                ),
            )
        assertEquals(2, events.size)
    }

    @Test
    fun simpleBefore() {
        val events =
            callEvaluator(
                BeforeExpression("startDate", "2023-05-27"),
                listOf(
                    entry("event1", "2023-05-25T00:00:00+02:00"),
                    entry("event2", "2023-05-28T01:00:00+02:00"),
                    entry("event3", "2023-05-29T00:00:00+02:00"),
                ),
                Clock.fixed(OffsetDateTime.parse("2024-05-31T01:00:00Z").toInstant(), ZoneId.of("Europe/Vienna")),
            )
        assertEquals(1, events.size)
        assertEquals("2023-05-25T00:00:00+02:00", events.first()["startDate"])
    }

    @Test
    fun simpleAfter() {
        val events =
            callEvaluator(
                AfterExpression("startDate", "2023-05-27"),
                listOf(
                    entry("event1", "2023-05-25T00:00:00+02:00"),
                    entry("event2", "2023-05-26T00:00:00+02:00"),
                    entry("event3", "2023-05-29T00:00:00+02:00"),
                ),
                Clock.fixed(OffsetDateTime.parse("2024-05-31T01:00:00Z").toInstant(), ZoneId.of("Europe/Vienna")),
            )
        assertEquals(1, events.size)
        assertEquals("2023-05-29T00:00:00+02:00", events.first()["startDate"])
    }

    @Test
    fun simpleBeforeWithNewFormat() {
        val events =
            callEvaluator(
                BeforeExpression("startDate", "2023-05-27"),
                listOf(
                    entryWithNewFormat("event1", "2023-05-25T00:00:00+02:00"),
                    entryWithNewFormat("event2", "2023-05-28T01:00:00+02:00"),
                    entryWithNewFormat("event3", "2023-05-29T00:00:00+02:00"),
                ),
                Clock.fixed(OffsetDateTime.parse("2024-05-31T01:00:00Z").toInstant(), ZoneId.of("Europe/Vienna")),
            )
        assertEquals(1, events.size)
        assertEquals("2023-05-25T00:00:00+02:00", events.first()["startDate:format=date"])
    }

    @Test
    fun simpleAfterWithNewFormat() {
        val events =
            callEvaluator(
                AfterExpression("startDate", "2023-05-27"),
                listOf(
                    entryWithNewFormat("event1", "2023-05-25T00:00:00+02:00"),
                    entryWithNewFormat("event2", "2023-05-26T00:00:00+02:00"),
                    entryWithNewFormat("event3", "2023-05-29T00:00:00+02:00"),
                ),
                Clock.fixed(OffsetDateTime.parse("2024-05-31T01:00:00Z").toInstant(), ZoneId.of("Europe/Vienna")),
            )
        assertEquals(1, events.size)
        assertEquals("2023-05-29T00:00:00+02:00", events.first()["startDate:format=date"])
    }

    @Test
    fun simpleAfterInclusiveToday() {
        val events =
            callEvaluator(
                AfterExpression("startDate", "2023-05-25"),
                listOf(
                    entry("event1", "2023-05-24T20:00:00Z"),
                    entry("event2", "2023-05-24T22:00:00Z"),
                    entry("event3", "2023-05-25T00:00:00Z"),
                    entry("event4", "2023-05-25T04:00:00Z"),
                    entry("event5", "2023-05-29T04:00:00Z"),
                ),
                Clock.fixed(OffsetDateTime.parse("2024-05-31T01:00:00Z").toInstant(), ZoneId.of("Europe/Vienna")),
            )
        assertEquals(4, events.size)
    }

    @Test
    fun simpleBeforeInclusiveToday() {
        val events =
            callEvaluator(
                BeforeExpression("startDate", "2023-05-29"),
                listOf(
                    entry("event1", "2023-05-25T04:00:00Z"),
                    entry("event2", "2023-05-28T22:00:00Z"),
                    entry("event3", "2023-05-30T00:00:00Z"),
                    entry("event4", "2023-05-30T04:00:00Z"),
                ),
                Clock.fixed(OffsetDateTime.parse("2024-05-31T01:00:00Z").toInstant(), ZoneId.of("Europe/Vienna")),
            )
        assertEquals(2, events.size)
    }

    @Test
    fun durationLonger() {
        val events =
            callEvaluator(
                DurationLongerExpression("startDate", "endDate", 2.0),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                        SemanticKeys.STARTDATE to "2024-05-31T00:00:00Z",
                        SemanticKeys.ENDDATE to "2024-05-31T03:00:00Z",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.STARTDATE to "2024-05-31T00:00:00Z",
                        SemanticKeys.ENDDATE to "2024-05-31T00:00:00Z",
                    ),
                ),
            )
        assertEquals(1, events.size)
        assertEquals("event1", events.first()["name"])
    }

    @Test
    fun durationShorter() {
        val events =
            callEvaluator(
                DurationShorterExpression("startDate", "endDate", 2.0),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                        SemanticKeys.STARTDATE to "2024-05-31T00:00:00Z",
                        SemanticKeys.ENDDATE to "2024-05-31T03:00:00Z",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.STARTDATE to "2024-05-31T00:00:00Z",
                        SemanticKeys.ENDDATE to "2024-05-31T00:00:00Z",
                    ),
                ),
            )
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    @Test
    fun durationLongerWithNewFormat() {
        val events =
            callEvaluator(
                DurationLongerExpression("startDate", "endDate", 2.0),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                        SemanticKeys.STARTDATE + ":format=date" to "2024-05-31T00:00:00Z",
                        SemanticKeys.ENDDATE + ":format=date" to "2024-05-31T03:00:00Z",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.STARTDATE + ":format=date" to "2024-05-31T00:00:00Z",
                        SemanticKeys.ENDDATE + ":format=date" to "2024-05-31T00:00:00Z",
                    ),
                ),
            )
        assertEquals(1, events.size)
        assertEquals("event1", events.first()["name"])
    }

    @Test
    fun durationShorterWithNewFormat() {
        val events =
            callEvaluator(
                DurationShorterExpression("startDate", "endDate", 2.0),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                        SemanticKeys.STARTDATE + ":format=date" to "2024-05-31T00:00:00Z",
                        SemanticKeys.ENDDATE + ":format=date" to "2024-05-31T03:00:00Z",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.STARTDATE + ":format=date" to "2024-05-31T00:00:00Z",
                        SemanticKeys.ENDDATE + ":format=date" to "2024-05-31T00:00:00Z",
                    ),
                ),
            )
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    @Test
    fun durationZero() {
        val events =
            callEvaluator(
                DurationLongerExpression("startDate", "endDate", 0.0),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                    ),
                ),
            )
        assertEquals(1, events.size)
        assertEquals("event1", events.first()["name"])
    }

    @Test
    fun hasField() {
        val events =
            callEvaluator(
                HasFieldExpression("recurrence.type"),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.RECURRENCE_TYPE to "REGULARLY",
                    ),
                ),
            )
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    @Test
    fun hasFieldWithKeySelector() {
        val events =
            callEvaluator(
                HasFieldExpression("*:format=date"),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.STARTDATE + ":format=date" to "2024-05-31T00:00:00Z",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event3",
                        "random:format=date" to "2024-05-31T00:00:00Z",
                    ),
                ),
            )
        assertEquals(2, events.size)
        assertEquals("event3", events[0]["name"])
        assertEquals("event2", events[1]["name"])
    }

    @Test
    fun resultsAreSorted() {
        val events =
            callEvaluator(
                ContainsExpression("name", "event"),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event3",
                        SemanticKeys.STARTDATE to
                            OffsetDateTime.now().minusDays(1)
                                .format(DateTimeFormatter.ISO_DATE_TIME),
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event1",
                        SemanticKeys.STARTDATE to
                            OffsetDateTime.now().plusDays(1)
                                .format(DateTimeFormatter.ISO_DATE_TIME),
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.STARTDATE to
                            OffsetDateTime.now()
                                .format(DateTimeFormatter.ISO_DATE_TIME),
                    ),
                ),
            )
        assertEquals(3, events.size)
        assertEquals("event3", events[0]["name"])
        assertEquals("event2", events[1]["name"])
        assertEquals("event1", events[2]["name"])
    }

    @Test
    fun isInNextSeconds() {
        val events =
            callEvaluator(
                IsInNextSecondsExpression(SemanticKeys.STARTDATE + ":format=date", 3600),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.STARTDATE + ":format=date" to "2024-05-31T00:30:00Z",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event3",
                        "random:format=date" to "2024-05-31T01:30:00Z",
                    ),
                ),
                Clock.fixed(OffsetDateTime.parse("2024-05-31T00:00:00Z").toInstant(), ZoneId.of("Europe/Vienna")),
            )
        assertEquals(1, events.size)
        assertEquals("event2", events[0]["name"])
    }

    @Test
    fun isInLastSeconds() {
        val events =
            callEvaluator(
                IsInLastSecondsExpression(SemanticKeys.STARTDATE + ":format=date", 3600),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event2",
                        SemanticKeys.STARTDATE + ":format=date" to "2024-05-31T00:30:00Z",
                    ),
                    mapOf(
                        SemanticKeys.NAME to "event3",
                        "random:format=date" to "2024-05-31T01:30:00Z",
                    ),
                ),
                Clock.fixed(OffsetDateTime.parse("2024-05-31T01:00:00Z").toInstant(), ZoneId.of("Europe/Vienna")),
            )
        assertEquals(1, events.size)
        assertEquals("event2", events[0]["name"])
    }

    private fun callEvaluator(expression: Expression): Collection<Entry> = callEvaluator(expression, testData())

    private fun callEvaluator(expression: Expression, entries: Collection<Entry>, clock: Clock = Clock.systemDefaultZone()): List<Entry> = createEvaluator(entries, clock)
        .evaluate(expression, PAGE_ALL)
        .result

    abstract fun createEvaluator(entries: Collection<Map<String, String>>, clock: Clock = Clock.systemDefaultZone()): Evaluator

    private fun testData(): Collection<Entry> = listOf(
        entry("name" to "event1", "field" to "value1"),
        entry("name" to "event2", "field" to "value2"),
        entry("name" to "somethingelse", "field" to "wuuut"),
        entry("name" to "somethingelse2", "field" to "wuuut"),
        entry("name" to "listyEvent1", "list:format=list" to "val1,val2"),
        entry("name" to "listyEvent2", "list:format=list" to "val3,val4"),
    )

    private fun entryWithNewFormat(name: String, startDate: String): Entry = entry(
        "name" to name,
        "startDate:format=date" to DateTimeFormatter.ISO_DATE_TIME.format(parseLocalDate(startDate)),
    )

    private fun entry(name: String, startDate: String): Entry = entry("name" to name, "startDate" to DateTimeFormatter.ISO_DATE_TIME.format(parseLocalDate(startDate)))

    private fun entry(vararg data: Pair<String, String>): Entry = data.toMap()

    private fun parseLocalDate(startDateAsString: String): OffsetDateTime = OffsetDateTime.parse(startDateAsString)
}
