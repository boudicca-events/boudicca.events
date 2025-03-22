package base.boudicca.model.structured.dsl

import base.boudicca.format.*
import base.boudicca.model.structured.Variant
import base.boudicca.model.structured.VariantConstants
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun textFormat() = TextFormatAdapter()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun numberFormat() = NumberFormatAdapter()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun dateFormat(): DateFormatAdapter = DateFormatAdapter()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun listFormat(): ListFormatAdapter = ListFormatAdapter()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun markdownFormat(): MarkdownFormatAdapter = MarkdownFormatAdapter()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun lang(value: String): Variant = Variant(VariantConstants.LANGUAGE_VARIANT_NAME, value)

// TODO: add json, enum and url format after discussing
// TODO: discuss in https://github.com/boudicca-events/boudicca.events/issues/662 how to handle this
