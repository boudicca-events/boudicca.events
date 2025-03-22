package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class BooleanFormatAdapter :
    AbstractFormatAdapter<Boolean>(VariantConstants.FormatVariantConstants.BOOLEAN_FORMAT_NAME) {
    override fun fromString(value: String): Boolean = value.toBoolean()

    override fun convertToString(value: Boolean): String = value.toString()
}
