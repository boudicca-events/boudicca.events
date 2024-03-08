package base.boudicca.search.query

import base.boudicca.SemanticKeys
import base.boudicca.search.service.query.PAGE_ALL
import base.boudicca.search.service.query.QueryException
import base.boudicca.search.service.query.QueryParser
import base.boudicca.search.service.query.evaluator.EvaluatorUtil
import base.boudicca.search.service.query.evaluator.SimpleEvaluator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QueryTest {

    @Test
    fun emptyQuery() {
        assertThrows<QueryException> {
            evaluateQuery("")
        }
    }

    @Test
    fun simpleEquals() {
        val events = evaluateQuery(""" "name" equals "event1" """)
        assertEquals(1, events.size)
        assertEquals("event1", events.first()["name"])
    }

    @Test
    fun simpleAnd() {
        val events = evaluateQuery(""" "name" contains "event" and "field" contains "2" """)
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    @Test
    fun simpleGrouping() {
        val events = evaluateQuery(""" ("name" contains "event") and ("field" contains "2") """)
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    @Test
    fun bigQuery() {
        val events =
            evaluateQuery(""" ((not "name" contains "event") or ( "field" contains "2") ) and "field" contains "a\\longer" """)
        assertEquals(1, events.size)
        assertEquals("somethingelse3", events.first()["name"])
    }

    @Test
    fun queryWithTimeLimits() {
        val events =
            evaluateQuery(""" "startDate" after "2023-05-27" and "startDate" before "2023-05-30" """)
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    @Test
    fun queryWithDurationLonger() {
        val events =
            evaluateQuery(""" duration "startDate" "endDate" longer 2 """)
        assertEquals(1, events.size)
        assertEquals("event1", events.first()["name"])
    }

    @Test
    fun queryWithDurationShorter() {
        val events =
            evaluateQuery(""" duration "startDate" "endDate" shorter 2 """)
        assertFalse(events.map { it["name"] }.contains("event1"))
    }

    @Test
    fun queryWithHasField() {
        val events =
            evaluateQuery(""" hasField "recurrence" """)
        assertEquals(1, events.size)
        assertEquals("event2", events.first()["name"])
    }

    private fun evaluateQuery(string: String): Collection<Map<String, String>> {
        return SimpleEvaluator(testData())
            .evaluate(QueryParser.parseQuery(string), PAGE_ALL)
            .result
    }

    private fun testData(): Collection<Map<String, String>> {
        return listOf(
            mapOf(
                "name" to "event1",
                "field" to "value1",
                SemanticKeys.STARTDATE to "2023-05-26T00:00:00Z",
                SemanticKeys.ENDDATE to "2023-05-26T03:00:00Z",
                SemanticKeys.TYPE to "konzert"
            ),
            mapOf(
                "name" to "event2",
                "field" to "value2",
                SemanticKeys.STARTDATE to "2023-05-29T00:00:00Z",
                SemanticKeys.TYPE to "theater",
                SemanticKeys.RECURRENCE to "yes"
            ),
            mapOf(
                "name" to "somethingelse", "field" to "wuuut",
                SemanticKeys.STARTDATE to "2023-05-31T00:00:00Z"
            ),
            mapOf(
                "name" to "somethingelse2",
                "field" to "wuuut",
                SemanticKeys.STARTDATE to "2024-05-31T00:00:00Z",
                SemanticKeys.TYPE to "konzert"
            ),
            mapOf(
                "name" to "somethingelse3",
                "field" to "this is a\\longer text",
                SemanticKeys.STARTDATE to "2024-05-31T00:00:00Z"
            ),
        )
    }
}