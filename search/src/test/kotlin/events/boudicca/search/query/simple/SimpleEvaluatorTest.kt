package events.boudicca.search.query.simple

import events.boudicca.SemanticKeys
import events.boudicca.search.query.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
            callEvaluator(BeforeExpression("2023-05-27"),
                listOf(
                    mapOf(SemanticKeys.STARTDATE to "2023-05-25T00:00:00"),
                    mapOf(SemanticKeys.STARTDATE to "2023-05-29T00:00:00"),
                ))
        assertEquals(1, events.size)
        assertEquals("2023-05-25T00:00:00", events.first()[SemanticKeys.STARTDATE])
    }

    @Test
    fun simpleAfter() {
        val events =
            callEvaluator(AfterExpression("2023-05-27"),
                listOf(
                    mapOf(SemanticKeys.STARTDATE to "2023-05-25T00:00:00"),
                    mapOf(SemanticKeys.STARTDATE to "2023-05-29T00:00:00"),
                ))
        assertEquals(1, events.size)
        assertEquals("2023-05-29T00:00:00", events.first()[SemanticKeys.STARTDATE])
    }

    private fun callEvaluator(expression: Expression): Collection<Map<String, String>> {
        return callEvaluator(expression, testData())
    }

    private fun callEvaluator(
        expression: Expression,
        events: Collection<Map<String, String>>
    ): Collection<Map<String, String>> {
        return SimpleEvaluator(events).evaluate(expression)
    }

    private fun testData(): Collection<Map<String, String>> {
        return listOf(
            mapOf("name" to "event1", "field" to "value1"),
            mapOf("name" to "event2", "field" to "value2"),
            mapOf("name" to "somethingelse", "field" to "wuuut"),
            mapOf("name" to "somethingelse2", "field" to "wuuut"),
        )
    }
}