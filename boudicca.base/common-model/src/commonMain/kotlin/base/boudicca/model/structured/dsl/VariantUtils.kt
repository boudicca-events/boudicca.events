package base.boudicca.model.structured.dsl

import base.boudicca.format.*
import base.boudicca.model.structured.Variant
import base.boudicca.model.structured.VariantConstants

fun textFormat() = TextFormatAdapter()

fun numberFormat() = NumberFormatAdapter()

fun dateFormat(): DateFormatAdapter = DateFormatAdapter()

fun listFormat(): ListFormatAdapter = ListFormatAdapter()

fun markdownFormat(): MarkdownFormatAdapter = MarkdownFormatAdapter()

fun lang(value: String): Variant = Variant(VariantConstants.LANGUAGE_VARIANT_NAME, value)

// TODO: add json, enum and url format after discussing
// TODO: discuss in https://github.com/boudicca-events/boudicca.events/issues/662 how to handle this
