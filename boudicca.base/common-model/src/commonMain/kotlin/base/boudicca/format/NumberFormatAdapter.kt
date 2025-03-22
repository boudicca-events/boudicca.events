package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * parsing utils to get string values to format number and back
 *
 * valid numbers depend on the underlying platform
 *
 * all methods may throw exceptions on wrong formatted values
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class NumberFormatAdapter : AbstractFormatAdapter<Number>(VariantConstants.FormatVariantConstants.NUMBER_FORMAT_NAME) {
    override fun fromString(value: String): Number {
        try {
            return NumberParser.parseNumber(value)
        } catch (e: Exception) {
            throw IllegalArgumentException("could not parse string $value to number")
        }
    }

    override fun convertToString(value: Number): String {
        return value.toString()
    }
}
