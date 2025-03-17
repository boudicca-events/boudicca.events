package base.boudicca.model.structured

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import base.boudicca.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

class FormatAdapterTest {

    @Suppress("detekt:EnumNaming")
    enum class TestEnum {
        // caPItaLIzAtIOn HeRE iS ON purPOSE, pls do not change
        asdf,
        Bsdf,
        CSDF_TEST
    }

    @Test
    fun `BooleanProperty should be able to convert to data value and back`() {
        val boolTrueStr = "true"
        val boolFalseStr = "false"
        val prop = BooleanProperty("test")

        val trueValue = prop.parseFromString(boolTrueStr)
        val falseValue = prop.parseFromString(boolFalseStr)

        assertThat(trueValue).isTrue()
        assertThat(falseValue).isFalse()

        val strTrueValue = prop.parseToString(trueValue)
        val strFalseValue = prop.parseToString(falseValue)

        assertThat(strTrueValue).isEqualTo(boolTrueStr)
        assertThat(strFalseValue).isEqualTo(boolFalseStr)
    }

    @Test
    fun ` should be able to convert to date value and back`() {
        val date = LocalDateTime.of(2025, 3, 8, 0, 45, 25, 0).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()

        val prop = DateProperty("test")
        val dateStr = prop.parseToString(date)
        val dateValue = prop.parseFromString(dateStr)

        assertThat(dateValue).isEqualTo(date)
    }

    @Test
    fun `EnumProperty should be able to convert to data value and back`() {
        val prop = EnumProperty("test", TestEnum::class.java)

        assertThat(prop.parseFromString(prop.parseToString(TestEnum.asdf))).isEqualTo(TestEnum.asdf)
        assertThat(prop.parseFromString(prop.parseToString(TestEnum.Bsdf))).isEqualTo(TestEnum.Bsdf)
        assertThat(prop.parseFromString(prop.parseToString(TestEnum.CSDF_TEST))).isEqualTo(TestEnum.CSDF_TEST)
    }

    @Test
    fun `ListProperty should be able to convert to data value and back`() {
        val listText = "hello,world,foo,bar,baz"
        val prop = ListProperty("test")

        val listValue = prop.parseFromString(listText)

        assertThat(listValue).hasSize(5)

        val strValue = prop.parseToString(listValue)

        assertThat(strValue).isEqualTo(listText)
    }

    @Test
    fun `MarkdownProperty should be able to convert to data value and back`() {
        val mdText = """
            # this is a multiline markdown text
            
            the idea of this text is to encode and decode the text
            without a loss.
            since this is a text property under the hood it should be easy
            right? Right? RIGHT?
        """.trimIndent()

        val prop = MarkdownProperty("test")

        val textValue = prop.parseFromString(mdText)
        val mdStrValue = prop.parseToString(textValue)

        assertThat(mdStrValue).isEqualTo(mdText)
    }

    @Test
    fun `NumberProperty should be able to convert to data value and back`() {
        val num = 135.486

        val prop = NumberProperty("test")

        val numStrValue = prop.parseToString(num)
        val numValue = prop.parseFromString(numStrValue)

        assertThat(numValue.toFloat()).isEqualTo(num.toFloat())
    }

    @Test
    fun `TextProperty should be able to convert to data value and back`() {
        val textStr = "do we really need to test this?"
        val prop = TextProperty("test")

        val textValue = prop.parseFromString(textStr)
        val strValue = prop.parseToString(textValue)

        assertThat(strValue).isEqualTo(textStr)
    }

    @Test
    fun `UriProperty should be able to convert to data value and back`() {
        val uriString = "https://xkcd.com/2928/"
        val prop = UriProperty("test")

        val uriValue = prop.parseFromString(uriString)
        val strValue = prop.parseToString(uriValue)

        assertThat(strValue).isEqualTo(uriString)
    }


}
