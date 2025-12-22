package base.boudicca.query.evaluator.util

import base.boudicca.model.Entry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FullTextIndexTest {
    @Test
    fun emptyCreate() {
        val index = create(listOf(), "name")

        assertEquals(0, index.size())
    }

    @Test
    fun createWithNonValueField() {
        val index =
            create(
                listOf(
                    mapOf("key" to "value1"),
                    mapOf("key" to "value2"),
                    mapOf("key" to "value3"),
                ),
                "name",
            )

        assertEquals(0, index.size())
    }

    @Test
    fun createSimple() {
        val index =
            create(
                listOf(
                    mapOf("name" to "value", "description" to "ignored"),
                ),
                "name",
            )

        assertEquals(5, index.size())
        assertEquals(Pair(0, 1), index.get(0))
        assertEquals(Pair(0, 4), index.get(1))
        assertEquals(Pair(0, 2), index.get(2))
        assertEquals(Pair(0, 3), index.get(3))
        assertEquals(Pair(0, 0), index.get(4))
    }

    @Test
    fun createSimpleWithTwoEntries() {
        val index =
            create(
                listOf(
                    mapOf("name" to "2"),
                    mapOf("name" to "1"),
                ),
                "name",
            )

        assertEquals(2, index.size())
        assertEquals(Pair(1, 0), index.get(0))
        assertEquals(Pair(0, 0), index.get(1))
    }

    @Test
    fun createTwoSameValues() {
        val index =
            create(
                listOf(
                    mapOf("name" to "1"),
                    mapOf("name" to "1"),
                ),
                "name",
            )

        assertEquals(1, index.size())
        assertEquals(0, index.get(0).second)
        assertEquals(bitsetOf(0, 1), index.getEntriesForWord(index.get(0).first))
    }

    @Test
    fun createTwoOneEmptyValues() {
        val index =
            create(
                listOf(
                    mapOf("name" to ""),
                    mapOf("name" to "1"),
                ),
                "name",
            )

        assertEquals(1, index.size())
        assertEquals(Pair(0, 0), index.get(0))
        assertEquals(bitsetOf(1), index.getEntriesForWord(0))
    }

    @Test
    fun caseInsensitiveOrder() {
        val index =
            create(
                listOf(
                    mapOf("name" to "a"),
                    mapOf("name" to "B"),
                ),
                "name",
            )

        assertEquals(2, index.size())
        assertEquals(Pair(0, 0), index.get(0))
        assertEquals(Pair(1, 0), index.get(1))
    }

    @Test
    fun simpleContainsSearch() {
        val index =
            create(
                listOf(
                    mapOf("name" to "a"),
                    mapOf("name" to "B"),
                ),
                "name",
            )
        val result = index.containsSearch("a")

        assertEquals(1, result.cardinality())
        assertTrue(result.get(0))
    }

    @Test
    fun twoValueBeginningContainsSearch() {
        val index =
            create(
                listOf(
                    mapOf("name" to "this is a name"),
                    mapOf("name" to "this is another name"),
                    mapOf("name" to "whatever"),
                ),
                "name",
            )
        val result = index.containsSearch("this is")

        assertEquals(2, result.cardinality())
        assertTrue(result.get(0))
        assertTrue(result.get(1))
    }

    @Test
    fun twoValueEndContainsSearch() {
        val index =
            create(
                listOf(
                    mapOf("name" to "this is a name"),
                    mapOf("name" to "this is another name"),
                    mapOf("name" to "whatever"),
                ),
                "name",
            )
        val result = index.containsSearch("name")

        assertEquals(2, result.cardinality())
        assertTrue(result.get(0))
        assertTrue(result.get(1))
    }

    @Test
    fun twoValueMiddleContainsSearch() {
        val index =
            create(
                listOf(
                    mapOf("name" to "this is a name"),
                    mapOf("name" to "this is another name"),
                    mapOf("name" to "whatever"),
                ),
                "name",
            )
        val result = index.containsSearch("is a")

        assertEquals(2, result.cardinality())
        assertTrue(result.get(0))
        assertTrue(result.get(1))
    }

    @Test
    fun weirdValuesContainsSearch() {
        val index =
            create(
                listOf(
                    mapOf("name" to ""),
                    mapOf("name" to "TEST"),
                    mapOf("name" to "test 😘"),
                ),
                "name",
            )
        val result = index.containsSearch("test")

        assertEquals(2, result.cardinality())
        assertTrue(result.get(1))
        assertTrue(result.get(2))
    }

    @Test
    fun caseInsensitiveContainsSearch() {
        val index =
            create(
                listOf(
                    mapOf("name" to "teST"),
                ),
                "name",
            )
        val result = index.containsSearch("TESt")

        assertEquals(1, result.cardinality())
        assertTrue(result.get(0))
    }

    @Test
    fun nothingFoundContainsSearch() {
        val index =
            create(
                listOf(
                    mapOf("name" to "value"),
                    mapOf("name" to "value2"),
                ),
                "name",
            )
        val result = index.containsSearch("other")

        assertEquals(0, result.cardinality())
    }

    private fun create(entries: List<Entry>, field: String): FullTextIndex = FullTextIndex(entries, field)
}
