package base.boudicca.model.structured.dsl

import base.boudicca.format.*
import base.boudicca.model.structured.Variant
import base.boudicca.model.structured.VariantConstants

fun textFormat() = TextFormatAdapter()

fun numberFormat() = NumberFormatAdapter()

fun dateFormat(): DateFormatAdapter = DateFormatAdapter()

fun listFormat(): ListFormatAdapter = ListFormatAdapter()

// TODO
fun jsonFormat(): Variant {
    return Variant(VariantConstants.FORMAT_VARIANT_NAME, VariantConstants.FormatVariantConstants.JSON_FORMAT_NAME)
}

// TODO
//fun enumFormat(): Variant {
//    return EnumFormatAdapter
//}

fun markdownFormat(): MarkdownFormatAdapter = MarkdownFormatAdapter()

fun lang(value: String): Variant = Variant(VariantConstants.LANGUAGE_VARIANT_NAME, value)

