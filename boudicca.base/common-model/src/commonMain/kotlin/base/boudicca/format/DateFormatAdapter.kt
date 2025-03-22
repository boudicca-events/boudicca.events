package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * parsing utils to get string values to format date and back
 *
 * date format is (for now, lets see if we extend it) the DateTimeFormatter.ISO_DATE_TIME format
 *
 * all methods may throw exceptions on wrong formatted values
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class DateFormatAdapter :
    AbstractFormatAdapter<Date>(VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME) {
    override fun fromString(value: String): Date {
        try {
            return DateParser.parseDate(value)
        } catch (e: Exception) {
            throw IllegalArgumentException("could not parse string value $value into date", e)
        }
    }

    override fun convertToString(value: Date): String {
        return DateParser.dateToString(value)
    }
}
