package base.boudicca.query.evaluator

import base.boudicca.model.Entry
import base.boudicca.SemanticKeys
import base.boudicca.query.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SimpleEvaluatorTest {

    @Test
    fun simpleEquals() {
        val events = callEvaluator(EqualsExpression("name", "event1"))
        assertEquals(1, events.size)
        assertEquals("event1", events.first()["name"])
    }

    @Test
    fun simpleContains() {
        val events = callEvaluator(ContainsExpression("name", "event"))
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
        assertEquals(3, events.size)
    }

    @Test
    fun orAndNot() {
        val events = callEvaluator(
            OrExpression(
                NotExpression(
                    ContainsExpression("name", "event")
                ),
                AndExpression(
                    EqualsExpression("name", "event1"),
                    EqualsExpression("field", "value1")
                )
            )
        )
        assertEquals(3, events.size)
    }

    @Test
    fun testCaseInsensitiveMatching() {
        var events = callEvaluator(
            EqualsExpression("field", "value"),
            listOf(
                mapOf("field" to "value"),
                mapOf("field" to "VAlue"),
            )
        )
        assertEquals(2, events.size)

        events = callEvaluator(
            ContainsExpression("field", "value"),
            listOf(
                mapOf("field" to "1value2"),
                mapOf("field" to "1VAlue2"),
            )
        )
        assertEquals(2, events.size)
    }

    @Test
    fun testCaseSensitiveFieldNameMatching() {
        val events = callEvaluator(
            EqualsExpression("field", "value"),
            listOf(
                mapOf("field" to "value"),
                mapOf("FIELD" to "value"),
            )
        )
        assertEquals(1, events.size)
    }

    @Test
    fun testStarFieldName() {
        var events = callEvaluator(
            EqualsExpression("*", "value"),
            listOf(
                mapOf("field" to "value"),
                mapOf("otherfield" to "value"),
            )
        )
        assertEquals(2, events.size)

        events = callEvaluator(
            ContainsExpression("*", "value"),
            listOf(
                mapOf("field" to "1value2"),
                mapOf("otherfield" to "1value2"),
            )
        )
        assertEquals(2, events.size)
    }

    @Test
    fun simpleBefore() {
        val events =
            callEvaluator(
                BeforeExpression("startDate", "2023-05-27"),
                listOf(
                    entry("event1", "2023-05-25T00:00:00"),
                    entry("event2", "2023-05-29T00:00:00"),
                )
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
                    entry("event1", "2023-05-25T00:00:00"),
                    entry("event2", "2023-05-29T00:00:00"),
                )
            )
        assertEquals(1, events.size)
        assertEquals("2023-05-29T00:00:00+02:00", events.first()["startDate"])
    }

    @Test
    fun simpleAfterInclusiveToday() {
        val events =
            callEvaluator(
                AfterExpression("startDate", "2023-05-25"),
                listOf(
                    entry("event1", "2023-05-25T00:00:00"),
                    entry("event2", "2023-05-29T00:00:00"),
                )
            )
        assertEquals(2, events.size)
    }

    @Test
    fun simpleBeforeInclusiveToday() {
        val events =
            callEvaluator(
                BeforeExpression("startDate", "2023-05-29"),
                listOf(
                    entry("event1", "2023-05-25T00:00:00"),
                    entry("event2", "2023-05-29T00:00:00"),
                )
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
                )
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
                )
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
                )
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
                )
            )
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }


    private fun callEvaluator(expression: Expression): Collection<Entry> {
        return callEvaluator(expression, testData())
    }

    private fun callEvaluator(
        expression: Expression,
        entries: Collection<Entry>
    ): List<Entry> {
        return SimpleEvaluator(entries)
            .evaluate(expression, PAGE_ALL)
            .result
    }

    private fun testData(): Collection<Entry> {
        return listOf(
            entry("name" to "event1", "field" to "value1"),
            entry("name" to "event2", "field" to "value2"),
            entry("name" to "somethingelse", "field" to "wuuut"),
            entry("name" to "somethingelse2", "field" to "wuuut"),
        )
    }

    private fun entry(name: String, startDate: String): Entry {
        return entry("name" to name, "startDate" to DateTimeFormatter.ISO_DATE_TIME.format(parseLocalDate(startDate)))
    }

    private fun entry(vararg data: Pair<String, String>): Entry {
        return data.toMap()
    }

    private fun parseLocalDate(startDateAsString: String): OffsetDateTime {
        return LocalDateTime.parse(startDateAsString, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("CET"))
            .toOffsetDateTime()
    }
}

