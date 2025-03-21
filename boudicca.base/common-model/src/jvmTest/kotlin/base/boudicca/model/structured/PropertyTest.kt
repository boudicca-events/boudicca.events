package base.boudicca.model.structured

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import base.boudicca.MarkdownProperty
import base.boudicca.SemanticKeys
import base.boudicca.TextProperty
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class PropertyTest {

    @Test
    fun `'toKeyString' should properly handle combinations of variants`() {
        assertThat(
            Key("name", emptyList()).toKeyString()
        ).isEqualTo("name")

        assertThat(
            Key(
                "name", listOf(
                    Variant("format", "markdown")
                )
            ).toKeyString()
        ).isEqualTo("name:format=markdown")

        assertThat(
            Key(
                "name", listOf(
                    Variant("format", "markdown"),
                    Variant("lang", "de")
                )
            ).toKeyString()
        ).isEqualTo("name:format=markdown:lang=de")
    }

    @Test
    fun `access to event data using property should work`() {

        val event = StructuredEvent(
            "testEvent", OffsetDateTime.now(), mapOf(
                Key(SemanticKeys.DESCRIPTION, emptyList()) to "test description",
                SemanticKeys.DESCRIPTION_MARKDOWN_PROPERTY.getKey() to "test description markdown"
            )
        )

        val result = event.getProperty(TextProperty("description"))

        assertThat(result).hasSize(1)
        assertThat(result.first().second).isEqualTo("test description")


        val result2 = event.getProperty(MarkdownProperty("description"))

        assertThat(result2).hasSize(1)
        assertThat(result2.first().second).isEqualTo("test description markdown")
    }

}
