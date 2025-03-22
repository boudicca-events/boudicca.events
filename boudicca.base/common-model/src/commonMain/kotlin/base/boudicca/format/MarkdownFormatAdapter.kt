package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class MarkdownFormatAdapter :
    AbstractFormatAdapter<String>(VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME) {
    override fun fromString(value: String): String = value

    override fun convertToString(value: String): String = value
}
