package base.boudicca.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ListFormatAdapterParseToStringTest {

    @Test
    fun testEmptyList() {
        assertThrows<IllegalArgumentException> { convertToString(listOf()) }
    }

    @Test
    fun testListWithEmptyString() {
        val result = convertToString(listOf(""))

        assertEquals("", result)
    }

    @Test
    fun testListWithOneValue() {
        val result = convertToString(listOf("value"))

        assertEquals("value", result)
    }

    @Test
    fun testListWithTwoValues() {
        val result = convertToString(listOf("value1", "value2"))

        assertEquals("value1,value2", result)
    }

    @Test
    fun testListWithValueWithComma() {
        val result = convertToString(listOf("val,ue"))

        assertEquals("val\\,ue", result)
    }

    @Test
    fun testListWithValueWithBackslash() {
        val result = convertToString(listOf("val\\ue"))

        assertEquals("val\\\\ue", result)
    }

    @Test
    fun testComplex() {
        val result = convertToString(listOf("1,", ",2", "\\3", "4\\"))

        assertEquals("1\\,,\\,2,\\\\3,4\\\\", result)
    }

    private fun convertToString(value: List<String>): String {
        return ListFormatAdapter().convertToString(value)
    }
}
