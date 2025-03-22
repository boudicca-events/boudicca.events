package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class TextFormatAdapter(
    formatVariantValue: String = VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME
) : AbstractFormatAdapter<String>(formatVariantValue) {
    override fun fromString(value: String): String = value

    override fun convertToString(value: String): String = value
}
