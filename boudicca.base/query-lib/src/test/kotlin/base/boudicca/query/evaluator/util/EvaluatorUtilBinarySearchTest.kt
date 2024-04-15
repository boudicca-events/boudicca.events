package base.boudicca.query.evaluator.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EvaluatorUtilBinarySearchTest {

    @Test
    fun testEmptyList() {
        assertEquals(-1, testWithList("a", listOf()))
    }

    @Test
    fun testSimpleSearch() {
        assertEquals(0, testWithList("a", listOf("a")))
    }

    @Test
    fun testSearch() {
        assertEquals(1, testWithList("b", listOf("a", "b", "c", "d", "e", "f")))
    }

    private fun testWithList(item: String, list: List<String>): Int {
        return EvaluatorUtil.binarySearch(0, list.size) { list[it].compareTo(item) }
    }


}