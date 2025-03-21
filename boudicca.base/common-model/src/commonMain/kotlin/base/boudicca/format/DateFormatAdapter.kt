package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import base.boudicca.model.OffsetDateTime

/**
 * parsing utils to get string values to format date and back
 *
 * date format is (for now, lets see if we extend it) the DateTimeFormatter.ISO_DATE_TIME format
 *
 * all methods may throw exceptions on wrong formatted values
 */
class DateFormatAdapter :
    AbstractFormatAdapter<OffsetDateTime>(VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME) {
    override fun fromString(value: String): OffsetDateTime {
        try {
            return OffsetDateTime.parseIsoDateTime(value)
        } catch (e: Exception) {
            throw IllegalArgumentException("could not parse string value $value into date", e)
        }
    }

    override fun convertToString(value: OffsetDateTime): String {
        return value.toIsoDateTimeString()
    }
}
