package events.boudicca.search.query

import events.boudicca.search.query.simple.SimpleEvaluator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QueryTest {

    @Test
    fun simpleEquals() {
        val events = evaluateQuery("name equals event1")
        assertEquals(1, events.size)
        assertEquals("event1", events.first()["name"])
    }

    @Test
    fun simpleAnd() {
        val events = evaluateQuery("name contains event and field contains 2")
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    @Test
    fun simpleGrouping() {
        val events = evaluateQuery("(name contains event) and (field contains 2)")
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    @Test
    fun bigQuery() {
        val events =
            evaluateQuery("((not name contains event) or ( field contains 2) ) and field contains \"a\\\\longer\"")
        assertEquals(1, events.size)
        assertEquals("somethingelse3", events.first()["name"])
    }

    private fun evaluateQuery(string: String): Collection<Map<String, String>> {
        return SimpleEvaluator(testData()).evaluate(QueryParser.parseQuery(string))
    }

    private fun testData(): Collection<Map<String, String>> {
        return listOf(
            mapOf("name" to "event1", "field" to "value1"),
            mapOf("name" to "event2", "field" to "value2"),
            mapOf("name" to "somethingelse", "field" to "wuuut"),
            mapOf("name" to "somethingelse2", "field" to "wuuut"),
            mapOf("name" to "somethingelse3", "field" to "this is a\\longer text"),
        )
    }
}