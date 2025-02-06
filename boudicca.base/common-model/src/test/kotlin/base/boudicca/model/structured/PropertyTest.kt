package base.boudicca.model.structured

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PropertyTest {

    @Test
    fun testToKeyString() {
        assertEquals("name", Key("name", emptyList()).toKeyString())
        assertEquals(
            "name:format=markdown",
            Key("name", listOf(Variant("format", "markdown"))).toKeyString()
        )
        assertEquals(
            "name:format=markdown:lang=de",
            Key("name", listOf(Variant("format", "markdown"), Variant("lang", "de"))).toKeyString()
        )
    }

}
