package base.boudicca.keyfilters

import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.toEvent
import base.boudicca.model.toStructuredEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KeyFiltersTest {
    @Test
    fun testEmptyKey() {
        val properties = filterKeys("doesnotmatter", emptyMap())

        assertTrue(properties.isEmpty())
    }

    @Test
    fun testSimplePropertyNameFilter() {
        val properties = filterKeys("name", testEntry())

        assertEquals(1, properties.size)
        assertEquals("name", properties[0].first)
    }

    @Test
    fun testSimplePropertyNameSortedCorrectly() {
        val properties = filterKeys("description", testEntry())

        assertEquals(4, properties.size)
        assertEquals("description", properties[0].first)
        assertEquals("description:format=markdown:lang=en", properties[1].first)
        assertEquals("description:lang=de", properties[2].first)
        assertEquals("description:lang=en", properties[3].first)
    }

    @Test
    fun testSelectAllPropertyNames() {
        val properties = filterKeys("*", testEntry())

        assertEquals(testEntry().size, properties.size)
    }

    @Test
    fun testSelectPropertyNamePlusVariant() {
        val properties = filterKeys("description:lang=de", testEntry())

        assertEquals(1, properties.size)
        assertEquals("description:lang=de", properties[0].first)
    }

    @Test
    fun testSelectMultiplePropertyNamePlusVariant() {
        val properties = filterKeys("description:lang=en", testEntry())

        assertEquals(2, properties.size)
        assertEquals("description:format=markdown:lang=en", properties[0].first)
        assertEquals("description:lang=en", properties[1].first)
    }

    @Test
    fun testSelectEmptyVariant() {
        val properties = filterKeys("description:lang=", testEntry())

        assertEquals(1, properties.size)
        assertEquals("description", properties[0].first)
    }

    @Test
    fun testSelectWildcardVariant() {
        val properties = filterKeys("description:lang=*", testEntry())

        assertEquals(3, properties.size)
        assertEquals("description:format=markdown:lang=en", properties[0].first)
        assertEquals("description:lang=de", properties[1].first)
        assertEquals("description:lang=en", properties[2].first)
    }

    @Test
    fun testMultipleVariantsWithZeroResult() {
        val properties = filterKeys("description:format=markdown:lang=de", testEntry())

        assertEquals(0, properties.size)
    }

    @Test
    fun testMultipleVariants() {
        val properties = filterKeys("description:format=markdown:lang=en", testEntry())

        assertEquals(1, properties.size)
        assertEquals("description:format=markdown:lang=en", properties[0].first)
    }

    @Test
    fun testKeysWithEvent() {
        val properties = filterKeys("name", testEntry().toEvent().get())

        assertEquals(1, properties.size)
        assertEquals("name", properties[0].first)
    }

    private fun assertEquals(keyString: String, key: Key){
        assertEquals(keyString, key.toKeyString())
    }

    private fun filterKeys(keyFilter: String, event: StructuredEvent): List<Pair<Key, String>> {
        return KeyFilters.filterKeys(Key.parse(keyFilter), event)
    }

    private fun filterKeys(keyFilter: String, entry: StructuredEntry): List<Pair<Key, String>> {
        return KeyFilters.filterKeys(Key.parse(keyFilter), entry)
    }

    private fun testEntry(): StructuredEntry {
        return mapOf(
            "name" to "My Event",
            "startDate" to "2024-04-27T23:59:00+02:00",
            "startDate:format=date" to "2024-04-27T23:59:00+02:00",
            "pictureUrl" to "https://i.insider.com/2b37544bfe5eb549a8378b00?width=1024",
            "description" to "my default lang description",
            "description:lang=de" to "meine deutsche beschreibung",
            "description:lang=en" to "my english description",
            "description:format=markdown:lang=en" to "#my english markdown description"
        ).toStructuredEntry()
    }
}
