package base.boudicca.model.structured

import assertk.assertThat
import assertk.assertions.isEqualTo
import base.boudicca.SemanticKeys
import org.junit.jupiter.api.Test

class KeyFilterBuilderTest {
    @Test
    fun testKeyFilterDsl() {
        val keyFilter =
            keyFilter(SemanticKeys.DESCRIPTION) {
                withVariant(
                    VariantConstants.FORMAT_VARIANT_NAME,
                    VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME,
                )
                withVariant(VariantConstants.LANGUAGE_VARIANT_NAME, "de")
            }

        assertThat(keyFilter).isEqualTo(KeyFilter.parse("description:format=markdown:lang=de"))
    }

    @Test
    fun testModifyKeyFilterDsl() {
        val keyFilter =
            keyFilter(SemanticKeys.DESCRIPTION) {
                withVariant(
                    VariantConstants.FORMAT_VARIANT_NAME,
                    VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME,
                )
            }

        assertThat(keyFilter).isEqualTo(KeyFilter.parse("description:format=markdown"))

        val modifiedKeyFilter =
            modifyKeyFilter(keyFilter) {
                withVariant(VariantConstants.LANGUAGE_VARIANT_NAME, "de")
            }

        assertThat(modifiedKeyFilter).isEqualTo(KeyFilter.parse("description:format=markdown:lang=de"))
    }
}
