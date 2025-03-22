package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class EnumFormatAdapter<E : Enum<E>>(private val toEnum: (value: String) -> E) :
    AbstractFormatAdapter<E>(VariantConstants.FormatVariantConstants.ENUM_FORMAT_NAME) {

    override fun convertToString(value: E): String {
        return value.name
    }

    override fun fromString(value: String): E {
        try {
            return toEnum(value)
        } catch (e: Exception) {
            throw IllegalArgumentException("error getting enum constant", e)
        }
    }
}
