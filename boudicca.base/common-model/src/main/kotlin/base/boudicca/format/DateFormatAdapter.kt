package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * parsing utils to get string values to format date and back
 *
 * date format is (for now, lets see if we extend it) the DateTimeFormatter.ISO_DATE_TIME format
 *
 * all methods may throw exceptions on wrong formatted values
 */
class DateFormatAdapter: AbstractFormatAdapter<OffsetDateTime>(VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME) {
    override fun fromString(value: String): OffsetDateTime {
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("could not parse string value $value into date", e)
        }
    }

    override fun convertToString(value: OffsetDateTime): String {
        return DateTimeFormatter.ISO_DATE_TIME.format(value)
    }
}
