package base.boudicca.search.query.evaluator

import base.boudicca.Event
import base.boudicca.SemanticKeys
import base.boudicca.search.service.query.*
import base.boudicca.search.service.query.evaluator.SimpleEvaluator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SimpleEvaluatorTest {

    @Test
    fun simpleEquals() {
        val events = callEvaluator(EqualsExpression("name", "event1"))
        assertEquals(1, events.size)
        assertEquals("event1", events.first().name)
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
            callEvaluatorWithEvents(
                BeforeExpression("2023-05-27"),
                listOf(
                    event("event1", "2023-05-25T00:00:00"),
                    event("event2", "2023-05-29T00:00:00"),
                )
            )
        assertEquals(1, events.size)
        assertEquals(parseLocalDate("2023-05-25T00:00:00"), events.first().startDate)
    }

    @Test
    fun simpleAfter() {
        val events =
            callEvaluatorWithEvents(
                AfterExpression("2023-05-27"),
                listOf(
                    event("event1", "2023-05-25T00:00:00"),
                    event("event2", "2023-05-29T00:00:00"),
                )
            )
        assertEquals(1, events.size)
        assertEquals(parseLocalDate("2023-05-29T00:00:00"), events.first().startDate)
    }

    @Test
    fun simpleAfterInclusiveToday() {
        val events =
            callEvaluatorWithEvents(
                AfterExpression("2023-05-25"),
                listOf(
                    event("event1", "2023-05-25T00:00:00"),
                    event("event2", "2023-05-29T00:00:00"),
                )
            )
        assertEquals(2, events.size)
    }

    @Test
    fun simpleBeforeInclusiveToday() {
        val events =
            callEvaluatorWithEvents(
                BeforeExpression("2023-05-29"),
                listOf(
                    event("event1", "2023-05-25T00:00:00"),
                    event("event2", "2023-05-29T00:00:00"),
                )
            )
        assertEquals(2, events.size)
    }

    @Test
    fun simpleIsMusic() {
        val events =
            callEvaluator(
                IsExpression("MUSIC"),
                listOf(
                    mapOf(SemanticKeys.TYPE to "konzert"),
                    mapOf(SemanticKeys.TYPE to "theater"),
                )
            )
        assertEquals(1, events.size)
        assertEquals("konzert", events.first().data[SemanticKeys.TYPE])
    }

    @Test
    fun simpleIsMusicIgnoreCase() {
        val events =
            callEvaluator(
                IsExpression("muSIC"),
                listOf(
                    mapOf(SemanticKeys.TYPE to "konzert"),
                    mapOf(SemanticKeys.TYPE to "theater"),
                )
            )
        assertEquals(1, events.size)
        assertEquals("konzert", events.first().data[SemanticKeys.TYPE])
    }

    @Test
    fun simpleIsOther() {
        val events =
            callEvaluator(
                IsExpression("other"),
                listOf(
                    mapOf(SemanticKeys.TYPE to "konzert"),
                    mapOf(SemanticKeys.TYPE to "theater"),
                    mapOf(SemanticKeys.TYPE to "whatever"),
                )
            )
        assertEquals(1, events.size)
        assertEquals("whatever", events.first().data[SemanticKeys.TYPE])
    }

    @Test
    fun durationLonger() {
        val events =
            callEvaluator(
                DurationLongerExpression(2.0),
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
        assertEquals("event1", events.first().name)
    }

    @Test
    fun durationShorter() {
        val events =
            callEvaluator(
                DurationShorterExpression(2.0),
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
        assertEquals("event2", events.first().name)
    }

    @Test
    fun durationZero() {
        val events =
            callEvaluator(
                DurationLongerExpression(0.0),
                listOf(
                    mapOf(
                        SemanticKeys.NAME to "event1",
                    ),
                )
            )
        assertEquals(1, events.size)
        assertEquals("event1", events.first().name)
    }


    private fun callEvaluator(expression: Expression): Collection<Event> {
        return callEvaluatorWithEvents(expression, testData())
    }

    private fun callEvaluator(
        expression: Expression,
        events: Collection<Map<String, String>>
    ): List<Event> {
        return callEvaluatorWithEvents(
            expression,
            events.map {
                Event(
                    it.getOrDefault(SemanticKeys.NAME, "name"),
                    if (it.containsKey(SemanticKeys.STARTDATE))
                        OffsetDateTime.parse(it[SemanticKeys.STARTDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
                    else
                        OffsetDateTime.now(),
                    it
                )
            })
    }

    private fun callEvaluatorWithEvents(
        expression: Expression,
        events: Collection<Event>
    ): List<Event> {
        return SimpleEvaluator(events)
            .evaluate(expression, PAGE_ALL)
            .result
    }

    private fun testData(): Collection<Event> {
        return listOf(
            event("event1", "field" to "value1"),
            event("event2", "field" to "value2"),
            event("somethingelse", "field" to "wuuut"),
            event("somethingelse2", "field" to "wuuut"),
        )
    }

    private fun event(name: String, vararg data: Pair<String, String>): Event {
        return Event(name, OffsetDateTime.now(), data.toMap())
    }

    private fun event(name: String, startDateAsString: String): Event {
        return Event(name, parseLocalDate(startDateAsString), emptyMap())
    }

    private fun parseLocalDate(startDateAsString: String): OffsetDateTime {
        return LocalDateTime.parse(startDateAsString, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("CET")).toOffsetDateTime()
    }
}

