package base.boudicca.format

import base.boudicca.model.structured.VariantConstants

class BooleanFormatAdapter :
    AbstractFormatAdapter<Boolean>(VariantConstants.FormatVariantConstants.BOOLEAN_FORMAT_NAME) {
    override fun fromString(value: String): Boolean = value.toBoolean()

    override fun convertToString(value: Boolean): String = value.toString()
}
