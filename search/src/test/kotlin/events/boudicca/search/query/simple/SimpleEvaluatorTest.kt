package events.boudicca.search.query.simple

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

    private fun callEvaluator(expression: Expression): Collection<Map<String, String>> {
        return SimpleEvaluator(testData()).evaluate(expression)
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