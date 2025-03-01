package base.boudicca.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import base.boudicca.SemanticKeys
import base.boudicca.model.structured.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class StructuredEventDSLTest {

    @Test
    fun `event dsl can be used to create structured event`() {

        val event = structuredEvent("testEvent", OffsetDateTime.now()) {
            withData(SemanticKeys.DESCRIPTION) {
                variant(
                    lang("en"),
                    markdownFormat(),
                    data = "# mydata"
                )
                variant(
                    lang("de"),
                    data = "MEINE DATEN!"
                )
            }
            withData(SemanticKeys.SOURCES) {
                variant(
                    listFormat(),
                    data = "[asdf.com, bsdf.com, csdf.com]"
                )
            }
        }

        assertThat(event.filterKeys(Key.parse("description")).size).isEqualTo(2)
        assertThat(event.filterKeys(Key.parse("description:lang=en")).size).isEqualTo(1)
        assertThat(event.filterKeys(Key.parse("description:lang=en")).first().second).isEqualTo("# mydata")
    }
}
