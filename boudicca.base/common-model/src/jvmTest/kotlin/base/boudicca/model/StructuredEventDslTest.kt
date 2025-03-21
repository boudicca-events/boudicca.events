package base.boudicca.model

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import base.boudicca.SemanticKeys
import base.boudicca.TextProperty
import base.boudicca.model.structured.KeyFilter
import base.boudicca.model.structured.dsl.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class StructuredEventDslTest {

    @Test
    fun `event dsl can be used to create structured event`() {
        val event = structuredEvent("testEvent", OffsetDateTime.now()) {
            withText(SemanticKeys.DESCRIPTION) {
                variant(
                    markdownFormat(),
                    lang("en"),
                    data = "# mydata"
                )
                variant(
                    lang("de"),
                    data = "MEINE DATEN!"
                )
                variant(
                    lang("de"),
                    data = listOf("MEINE DATEN!").toString()
                )
            }
            with(SemanticKeys.SOURCES, defaultFormatAdapter = listFormat()) {
                variant(
                    lang("de"),
                    data = listOf("asdf.com in german", "bsdf.com in german", "csdf.com in german")
                )
                variant(
                    lang("en"),
                    data = listOf("asdf.com in english", "bsdf.com in english", "csdf.com in english")
                )
                variant(
                    textFormat(),
                    data = "asdf.com"
                )
            }
        }

        assertAll {
            assertThat(event.filterKeys(KeyFilter.parse("description")).size).isEqualTo(2)
            assertThat(event.filterKeys(KeyFilter.parse("description:lang=en")).size).isEqualTo(1)
            assertThat(event.filterKeys(KeyFilter.parse("description:lang=en")).first().second).isEqualTo("# mydata")
            assertThat(event.filterKeys(KeyFilter.parse("sources")).size).isEqualTo(3)
            assertThat(event.filterKeys(KeyFilter.parse("sources:lang=*")).first().second).contains("asdf.com")
            assertThat(
                event.filterKeys(KeyFilter.parse("sources:lang=de")).first().second
            ).isEqualTo("asdf.com in german,bsdf.com in german,csdf.com in german")
            assertThat(
                event.filterKeys(KeyFilter.parse("sources:lang=en")).first().second
            ).isEqualTo("asdf.com in english,bsdf.com in english,csdf.com in english")
            assertThat(event.filterKeys(KeyFilter.parse("sources:format=list")).size).isEqualTo(2)
            assertThat(
                event.filterKeys(KeyFilter.parse("sources:format=list")).first().second
            ).isEqualTo("asdf.com in german,bsdf.com in german,csdf.com in german")
        }
    }

    @Test
    fun `event dsl compact form can be used to create structured event with less lines`() {
        val event = structuredEvent("testEvent", OffsetDateTime.now()) {
            withData(SemanticKeys.DESCRIPTION, markdownFormat(), "# mydata")
            withData(SemanticKeys.SOURCES, listFormat(), listOf("asdf.com", "bsdf.com", "csdf.com"))
        }

        assertAll {
            assertThat(event.filterKeys(KeyFilter.parse("description")).size).isEqualTo(1)
            assertThat(event.filterKeys(KeyFilter.parse("description:format=markdown")).size).isEqualTo(1)
            assertThat(
                event.filterKeys(KeyFilter.parse("description:format=markdown"))
                    .first().second
            ).isEqualTo("# mydata")
            assertThat(
                event.filterKeys(KeyFilter.parse("sources"))
                    .first().second
            ).isEqualTo("asdf.com,bsdf.com,csdf.com")
            assertThat(event.filterKeys(KeyFilter.parse("sources:format=list")).size).isEqualTo(1)
        }
    }

    @Test
    fun `event can be modified with event dsl`() {
        val event = structuredEvent("testEvent", OffsetDateTime.now()) {
            withData(SemanticKeys.DESCRIPTION, markdownFormat(), "# mydata")
            withData(SemanticKeys.SOURCES, listFormat(), listOf("asdf.com", "bsdf.com", "csdf.com"))
        }

        assertAll {
            assertThat(event.filterKeys(KeyFilter.parse("description:format=markdown")).size).isEqualTo(1)
            assertThat(
                event.filterKeys(KeyFilter.parse("description:format=markdown"))
                    .first().second
            ).isEqualTo("# mydata")
            assertThat(event.filterKeys(KeyFilter.parse("sources:format=list")).size).isEqualTo(1)
            assertThat(event.filterKeys(KeyFilter.parse("category")).size).isEqualTo(0)
        }

        val modifiedEvent = modify(event) {
            withTextData(SemanticKeys.DESCRIPTION, "# mymodifieddata")
            withTextData(SemanticKeys.CATEGORY, "testdata")
        }

        assertAll {
            assertThat(modifiedEvent.filterKeys(KeyFilter.parse("description:format=markdown")).size).isEqualTo(1)
            assertThat(
                modifiedEvent.filterKeys(KeyFilter.parse("description:format=markdown")).first().second
            ).isEqualTo("# mydata")
            val descriptionProperty = modifiedEvent.getProperty(TextProperty("description"))
            assertThat(descriptionProperty.first().second).isEqualTo("# mymodifieddata")
            assertThat(modifiedEvent.filterKeys(KeyFilter.parse("sources:format=list")).size).isEqualTo(1)
            assertThat(modifiedEvent.filterKeys(KeyFilter.parse("category")).size).isEqualTo(1)
            assertThat(modifiedEvent.filterKeys(KeyFilter.parse("category")).first().second).isEqualTo("testdata")
        }
    }

    @Test
    fun `event dsl can be used with property syntax`() {
        val event = structuredEvent("testEvent", OffsetDateTime.now()) {
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, "description text property value")
            withProperty(SemanticKeys.DESCRIPTION_MARKDOWN_PROPERTY, "# mydata")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf("asdf.com", "bsdf.com", "csdf.com"))
        }

        assertAll {
            assertThat(event.filterKeys(KeyFilter.parse("description:format=markdown")).size).isEqualTo(1)
            assertThat(
                event.filterKeys(KeyFilter.parse("description:format=markdown"))
                    .first().second
            ).isEqualTo("# mydata")
            assertThat(event.filterKeys(KeyFilter.parse("sources:format=list")).size).isEqualTo(1)
            assertThat(event.filterKeys(KeyFilter.parse("category")).size).isEqualTo(0)
        }

        val modifiedEvent = modify(event) {
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, "# mymodifieddata")
            withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.TECH)
        }

        assertAll {
            assertThat(modifiedEvent.filterKeys(KeyFilter.parse("description:format=markdown")).size).isEqualTo(1)
            assertThat(
                modifiedEvent.filterKeys(KeyFilter.parse("description:format=markdown")).first().second
            ).isEqualTo("# mydata")
            val descriptionProperty = modifiedEvent.getProperty(TextProperty("description"))
            assertThat(descriptionProperty.first().second).isEqualTo("# mymodifieddata")
            assertThat(modifiedEvent.filterKeys(KeyFilter.parse("sources:format=list")).size).isEqualTo(1)
            assertThat(modifiedEvent.filterKeys(KeyFilter.parse("category")).size).isEqualTo(1)
            assertThat(modifiedEvent.filterKeys(KeyFilter.parse("category")).first().second).isEqualTo("TECH")
        }
    }
}
