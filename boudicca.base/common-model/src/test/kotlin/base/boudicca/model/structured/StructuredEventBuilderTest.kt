package base.boudicca.model.structured

import assertk.assertThat
import assertk.assertions.isEqualTo
import base.boudicca.TextProperty
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class StructuredEventBuilderTest {

    @Test
    fun `minimal event with constructor`() {
        val name = "Event Name"
        val startDate = OffsetDateTime.now()

        val event = StructuredEvent(name, startDate)
        val builderEvent = StructuredEvent(name, startDate)

        assertThat(builderEvent).isEqualTo(event)
    }

    @Test
    fun `single data field with key value builder`() {
        val name = "Event Name"
        val startDate = OffsetDateTime.now()
        val key = Key("key1")
        val value = "value1"

        val event = StructuredEvent(name, startDate, mapOf<Key, String>(key to value))
        val builderEvent = StructuredEvent(name, startDate)
            .toBuilder()
            .withKeyValuePair(key, value)
            .build()

        assertThat(builderEvent).isEqualTo(event)
    }

    @Test
    fun `single data field with property builder`() {
        val name = "Event Name"
        val startDate = OffsetDateTime.now()
        val property = TextProperty("TestProperty")
        val value = "value1"

        val event = StructuredEvent(name, startDate, mapOf<Key, String>(property.getKey() to value))
        val builderEvent = StructuredEvent(name, startDate)
            .toBuilder()
            .withProperty(property, value)
            .build()

        assertThat(builderEvent).isEqualTo(event)
    }

    @Test
    fun `single data field with property builder and a variant`() {
        val name = "Event Name"
        val startDate = OffsetDateTime.now()
        val variant = Variant("variant1", "value2")
        val property = TextProperty("TestProperty")
        val value = "value1"
        val key = Key(property.getKey().name, listOf(variant))

        val event = StructuredEvent(name, startDate, mapOf<Key, String>(key to value))
        val builderEvent = StructuredEvent(name, startDate)
            .toBuilder()
            .withProperty(
                property = property,
                value = value,
                variants = listOf(variant)
            )
            .build()

        assertThat(builderEvent).isEqualTo(event)
    }
}
