package base.boudicca.query.evaluator.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleIndexTest {
    @Test
    fun createEmptyIndex() {
        createIndex(listOf())
    }

    @Test
    fun searchOneElement() {
        val index = createIndex(listOf("a"))

        assertEquals(bitsetOf(0), index.search { it.compareTo("a") })
    }

    @Test
    fun searchOneElementInTwo() {
        val index = createIndex(listOf("b", "a"))

        assertEquals(bitsetOf(1), index.search { it.compareTo("a") })
    }

    @Test
    fun searchTwoElements() {
        val index = createIndex(listOf("b", "a", "aa", "a", "asd", "c"))

        assertEquals(bitsetOf(1, 3), index.search { it.compareTo("a") })
    }

    @Test
    fun searchNullableIndex() {
        val index =
            SimpleIndex<String?>(listOf("c", null, "b", "a", null, null, null), Comparator.naturalOrder<String?>())

        assertEquals(bitsetOf(3), index.search { it?.compareTo("a") ?: -1 })
    }

    private fun createIndex(list: List<String>): SimpleIndex<String> {
        return SimpleIndex(list, Comparator.naturalOrder())
    }
}